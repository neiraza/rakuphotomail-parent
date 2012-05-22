/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.controller.MessagingController;
import jp.co.fttx.rakuphotomail.controller.MessagingListener;
import jp.co.fttx.rakuphotomail.mail.Address;
import jp.co.fttx.rakuphotomail.mail.Flag;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.Message.RecipientType;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.internet.MimeMessage;
import jp.co.fttx.rakuphotomail.mail.internet.TextBody;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.MessageSync;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoConnectivityCheck;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;

import java.util.Date;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySendingMail extends RakuPhotoActivity implements View.OnClickListener {

    private Account mAccount;
    private EditText mMessage;
    private TextView mToAddressAndName;
    private TextView mSentFlag;
    private Address mToAddress;
    private Address mFromAddress;
    private Button mSend;
    private MessageReference mMessageReference;
    private String mReferences;
    private String mInReplyTo;
    private boolean mAnswered;

    private static final String STATE_IN_REPLY_TO = "jp.co.fttx.rakuphotomail.activity.GallerySendingMail.inReplyTo";
    private static final String STATE_REFERENCES = "jp.co.fttx.rakuphotomail.activity.GallerySendingMail.references";

    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
    private static final String EXTRA_ADDRESS_FROM = "addressFrom";
    private static final String EXTRA_ADDRESS_FROM_NAME = "addressFromName";
    private static final String EXTRA_ADDRESS_TO = "addressTo";
    private static final String EXTRA_ADDRESS_TO_NAME = "addressToName";
    private static final String EXTRA_MESSAGE_ID = "messageId";
    private static final String EXTRA_MESSAGE_ANSWERED = "answered";

    private Listener mListener = new Listener();

    /**
     * @param icicle Activity起動時に使用
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.gallery_sending_mail);
        setupViews();
        final Intent intent = getIntent();
        initInfo(intent);
        setMSentFlagVisibility();
//        MessagingController.getInstance(getApplication()).addListener(mListener);
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onResume() {
        super.onResume();
        MessagingController.getInstance(getApplication()).addListener(mListener);
    }

    /**
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onPause() {
        super.onPause();
        MessagingController.getInstance(getApplication()).removeListener(mListener);
    }

    /**
     * 返信済みフラグ非表示.<br>
     * 本メールが以前返信済みか否かをmAnsweredの状態で判定し、表示をView.GONEにする。
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setMSentFlagVisibility() {
        if (!mAnswered) {
            mSentFlag.setVisibility(View.GONE);
        }
    }

    /**
     * 各Viewの初期設定を行う.
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setupViews() {
        mMessage = (EditText) findViewById(R.id.gallery_sending_mail_content);
        mToAddressAndName = (TextView) findViewById(R.id.gallery_sending_mail_to_address_and_name);
        mSend = (Button) findViewById(R.id.gallery_sending_mail_send);
        mSend.setOnClickListener(this);
        mSentFlag = (TextView) findViewById(R.id.gallery_sending_mail_sent_flag);
    }

    /**
     * Intentを元に初期設定を行う.
     *
     * @param intent 初期設定用のIntent
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void initInfo(Intent intent) {
        mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);

        final String accountUuid = (mMessageReference != null) ? mMessageReference.accountUuid : intent
                .getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        if (mAccount == null) {
            mAccount = Preferences.getPreferences(this).getDefaultAccount();
        }

        StringBuilder recipientTypeTo = new StringBuilder();

        String address = intent.getStringExtra(EXTRA_ADDRESS_TO);

        recipientTypeTo.append(address == null ? "" : toRfcMailAddress(address));
        if (!RakuPhotoStringUtils.isNotBlank(address)) {
            address = getString(R.string.common_message_unknown);
            Log.w(RakuPhotoMail.LOG_TAG, getString(R.string.sending_message_address_unknown));
            Toast.makeText(getApplicationContext(), getString(R.string.sending_message_address_unknown), Toast.LENGTH_LONG).show();
            mSend.setEnabled(false);
        }
        String addressName = intent.getStringExtra(EXTRA_ADDRESS_TO_NAME);

        recipientTypeTo.append(addressName == null ? "" : addressName);
        if (!RakuPhotoStringUtils.isNotBlank(addressName)) {
            addressName = getString(R.string.common_message_unknown);
        }
        setMToAddressName(address, addressName);
        mToAddress = new Address(recipientTypeTo.toString());

        setMFromAddress(intent.getStringExtra(EXTRA_ADDRESS_FROM),
                intent.getStringExtra(EXTRA_ADDRESS_FROM_NAME));

        mInReplyTo = intent.getStringExtra(EXTRA_MESSAGE_ID);
        mReferences = intent.getStringExtra(EXTRA_MESSAGE_ID);
        mAnswered = intent.getBooleanExtra(EXTRA_MESSAGE_ANSWERED, false);
    }

    private String toRfcMailAddress(String address) {
        StringBuilder tmp = new StringBuilder("<");
        tmp.append(address);
        tmp.append(">");
        return tmp.toString();
    }

    /**
     * 宛先アドレスと名前をTextViewに設定する.
     *
     * @param address address
     * @param name    name
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void setMToAddressName(String address, String name) {
        StringBuilder tmpName = new StringBuilder(name.trim());
        tmpName.append("(");
        tmpName.append(address);
        tmpName.append(")");
        mToAddressAndName.setText(tmpName);
    }

    /**
     * メール送信側のメールアドレス、送信者名を設定する.
     *
     * @param address  送信側のメールアドレス
     * @param personal 送信側の送信者名
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void setMFromAddress(String address, String personal) {
        mFromAddress = new Address(address, personal);
    }

    /**
     * @param outState bundle
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_IN_REPLY_TO, mInReplyTo);
        outState.putString(STATE_REFERENCES, mReferences);
    }

    /**
     * @param savedInstanceState saved
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mInReplyTo = savedInstanceState.getString(STATE_IN_REPLY_TO);
        mReferences = savedInstanceState.getString(STATE_REFERENCES);
    }

    /**
     * @param v View
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    @Override
    public void onClick(View v) {
        if (onCheck()) {
            if (RakuPhotoConnectivityCheck.isConnectivity(getApplicationContext())) {
                final String replyTargetUid = mMessageReference.uid;
                onSend(replyTargetUid);
                GallerySlideStop.actionHandle(this, mAccount, mAccount.getInboxFolderName(), replyTargetUid);
                finish();
            } else {
                Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#startMessageSyncTask ネットワーク接続が切れています");
                Toast.makeText(getApplicationContext(), "ネットワーク接続が切れています", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * メッセージ入力チェック.<br>
     * メッセージ未入力の場合、入力可能文字数以下の場合はfalseを返す.<br>
     * それ以外についてはtrueを返す.
     *
     * @return boolean. メッセージ再入力をさせる場合はfalse. それ以外はtrue.
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private boolean onCheck() {
        String message = mMessage.getText().toString();
        int len = message.length();
        if (0 == len) {
            Toast.makeText(this, "メッセージが入力されていません。", Toast.LENGTH_LONG).show();
            return false;
        } else if (17 < len) {
            Toast.makeText(this, "入力された本文「" + message + "」(" + len + "文字) は長すぎます。入力可能な文字数は17文字までです。",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * メールを送信. <br>
     * 送信後はMessagingControllerに送信済みを表すフラグをDBに書き込む処理を委譲する.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onSend(String uid) {
        sendMessage();
        MessagingController.getInstance(getApplication()).setFlag(mAccount, mAccount.getInboxFolderName(),
                new String[]{uid}, mMessageReference.flag, true);
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onSendAfter(Account account, String folderName, String uid) {
        try {
            MessageSync.sentMessageAfter(account, folderName, uid);
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
        } catch (RakuRakuException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
            return;
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void sendMessage() {
        new SendMessageTask(this).execute();
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class SendMessageTask extends AsyncTask<Void, Integer, Void> implements DialogInterface.OnCancelListener {
        ProgressDialog dialog;
        Context context;

        public SendMessageTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please wait");
            dialog.setMessage("Loading data...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(this);
            dialog.setMax(100);
            dialog.setProgress(0);
            dialog.show();
        }

        /**
         * @return null
         */
        @Override
        protected Void doInBackground(Void... params) {
            MimeMessage message;
            try {
                message = createMessage();
                publishProgress(20);
            } catch (MessagingException me) {
                Log.e(RakuPhotoMail.LOG_TAG, "Failed to create new message for send or save.", me);
                throw new RuntimeException("Failed to create a new message for send or save.", me);
            }
            String sendTempUid = MessagingController.getInstance(getApplication()).sendMessage(mAccount, message);
            publishProgress(60);
            onSendAfter(mAccount, mAccount.getSentFolderName(), sendTempUid);
            publishProgress(100);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            dialog.setProgress(values[0]);
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(Void tmp) {
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    /**
     * Build the message to be sent.
     *
     * @return Message to be sent.
     * @throws MessagingException uheee
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private MimeMessage createMessage() throws MessagingException {
        MimeMessage message = new MimeMessage();
        message.addSentDate(new Date());
        message.setFrom(mFromAddress);
        message.setRecipient(RecipientType.TO, mToAddress);
        message.setSubject(mMessage.getText().toString());
        message.setHeader("User-Agent", getString(R.string.message_header_mua));
        if (mInReplyTo != null) {
            message.setInReplyTo(mInReplyTo);
        }
        if (mReferences != null) {
            message.setReferences(mReferences);
        }
        TextBody body = null;
        body = new TextBody(getString(R.string.send_message_text));
        message.setBody(body);
        return message;
    }

    /**
     * Activity外部呼び出し用.
     *
     * @param context     context
     * @param messageBean message
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    public static void actionReply(Context context, MessageBean messageBean) {
        Intent i = new Intent(context, GallerySendingMail.class);
        i.putExtra(EXTRA_ADDRESS_TO, messageBean.getSenderAddress());
        i.putExtra(EXTRA_ADDRESS_TO_NAME, messageBean.getSenderName());
        i.putExtra(EXTRA_ADDRESS_FROM, messageBean.getToList());
        i.putExtra(EXTRA_ADDRESS_FROM_NAME, "");
        i.putExtra(EXTRA_MESSAGE_ID, messageBean.getMessageId());
        i.putExtra(EXTRA_MESSAGE_ANSWERED, messageBean.isFlagAnswered());
        MessageReference reference = messageBean.getMessage().makeMessageReference();
        reference.flag = Flag.ANSWERED;
        i.putExtra(EXTRA_MESSAGE_REFERENCE, reference);
        context.startActivity(i);
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    class Listener extends MessagingListener {
        /**
         * 未使用.
         *
         * @param account account info
         * @param folder  receive mail folder name
         * @param uid     message uid
         * @author tooru.oguri
         * @since 0.1-beta1
         */
        @Override
        public void loadMessageForViewStarted(Account account, String folder, String uid) {
            if ((mMessageReference == null) || !mMessageReference.uid.equals(uid)) {
                return;
            }
        }

        /**
         * 未使用.
         *
         * @param account account info
         * @param folder  receive mail folder name
         * @param uid     message uid
         * @author tooru.oguri
         * @since 0.1-beta1
         */
        @Override
        public void loadMessageForViewFinished(Account account, String folder, String uid, Message message) {
            if ((mMessageReference == null) || !mMessageReference.uid.equals(uid)) {
                return;
            }
        }

        /**
         * @param account account info
         * @param folder  receive mail folder name
         * @param uid     message uid
         * @author tooru.oguri
         * @since 0.1-beta1
         */
        @Override
        public void loadMessageForViewBodyAvailable(Account account, String folder, String uid,
                                                    final Message message) {
            if ((mMessageReference == null) || !mMessageReference.uid.equals(uid)) {
                return;
            }
            try {
                if (message.getMessageId() != null && message.getMessageId().length() > 0) {
                    mInReplyTo = message.getMessageId();

                    if (message.getReferences() != null && message.getReferences().length > 0) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < message.getReferences().length; i++)
                            stringBuilder.append(message.getReferences()[i]);

                        mReferences = stringBuilder.toString() + " " + mInReplyTo;
                    } else {
                        mReferences = mInReplyTo;
                    }
                }
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
            }
        }
    }
}
