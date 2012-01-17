/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import java.util.Date;

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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySendingMail extends RakuPhotoActivity implements View.OnClickListener {

    private Account mAccount;
    private EditText mMessage;
    private TextView mTo;
    private TextView mToName;
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

        setMToAddressVisibility();
        setMSentFlagVisibility();

        MessagingController.getInstance(getApplication()).addListener(mListener);
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
        mTo = (TextView) findViewById(R.id.gallery_sending_mail_to_address);
        mToName = (TextView) findViewById(R.id.gallery_sending_mail_to_name);
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

        setMToAddress(intent.getStringExtra(EXTRA_ADDRESS_TO));
        setMToAddressName(intent.getStringExtra(EXTRA_ADDRESS_TO_NAME));
        mToAddress = new Address(mTo.getText().toString(), mToName.getText().toString());

        setMFromAddress(intent.getStringExtra(EXTRA_ADDRESS_FROM),
                intent.getStringExtra(EXTRA_ADDRESS_FROM_NAME));

        mInReplyTo = intent.getStringExtra(EXTRA_MESSAGE_ID);
        mReferences = intent.getStringExtra(EXTRA_MESSAGE_ID);
        mAnswered = intent.getBooleanExtra(EXTRA_MESSAGE_ANSWERED, false);
    }

    /**
     * 宛先の表示名をメールアドレスか宛先名のいづれかを選択し表示する.<br>
     * 表示優先順位は「宛先名」、「メールアドレス」の順。
     *
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setMToAddressVisibility() {
        if ("".equals(mToName.getText().toString())) {
            mToName.setVisibility(View.GONE);
            mTo.setVisibility(View.VISIBLE);
        } else {
            mToName.setVisibility(View.VISIBLE);
            mTo.setVisibility(View.GONE);
        }
    }

    /**
     * 宛先メールアドレスをTextViewに設定する.
     *
     * @param addressTo 宛先メールアドレス
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void setMToAddress(String addressTo) {
        mTo.setText(addressTo);
    }

    /**
     * 宛先名をTextViewに設定する.
     *
     * @param addressToName address name
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void setMToAddressName(String addressToName) {
        mToName.setText(addressToName);
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
            onSend();
            finish();
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
    private void onSend() {
        Log.d("SendTest","GallerySendingMail#onSend start");
        sendMessage();
        final Account account = Preferences.getPreferences(this).getAccount(mMessageReference.accountUuid);
        final String folderName = mMessageReference.folderName;
        final String sourceMessageUid = mMessageReference.uid;
        Log.d("SendTest","GallerySendingMail#onSend folderName:" + folderName);
        Log.d("SendTest","GallerySendingMail#onSend sourceMessageUid:" + sourceMessageUid);
        MessagingController.getInstance(getApplication()).setFlag(account, folderName,
                new String[]{sourceMessageUid}, mMessageReference.flag, true);
        Log.d("SendTest","GallerySendingMail#onSend end");
    }

    /**
     * メールを送信.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void sendMessage() {
        new SendMessageTask().execute();
    }

    /**
     * メールを送信Class.
     *
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class SendMessageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.d("SendTest","SendMessageTask#doInBackground start");

            /*
             * Create the message from all the data the user has entered.
             */
            MimeMessage message;
            try {
                message = createMessage();
            } catch (MessagingException me) {
                Log.e(RakuPhotoMail.LOG_TAG, "Failed to create new message for send or save.", me);
                throw new RuntimeException("Failed to create a new message for send or save.", me);
            }

            Log.d("SendTest","SendMessageTask#doInBackground 送信前:message.getUid():" + message.getUid());
            MessagingController.getInstance(getApplication()).sendMessage(mAccount, message, null);

            Log.d("SendTest","SendMessageTask#doInBackground end");
            return null;
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
        body = new TextBody("このメールは、らくフォトメールの試験用メールです。");
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
        // XXX 送信者名は表示予定が無いため用意していない
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
                e.printStackTrace();
            }
        }
    }
}
