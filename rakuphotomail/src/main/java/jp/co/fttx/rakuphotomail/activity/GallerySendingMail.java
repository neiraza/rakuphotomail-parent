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
 * GallerySendingMail.
 * 
 * @author tooru.oguri
 * 
 */
public class GallerySendingMail extends RakuPhotoActivity implements
		View.OnClickListener {

	private Account mAccount;
	private EditText mMessage;
	private TextView mTo;
	private TextView mToName;
	private Address mToAddress;
	private Address mFromAddress;
	private Button mSend;
	private MessageReference mMessageReference;
	private String mReferences;
	private String mInReplyTo;

	private static final String STATE_IN_REPLY_TO = "jp.co.fttx.rakuphotomail.activity.GallerySendingMail.inReplyTo";
	private static final String STATE_REFERENCES = "jp.co.fttx.rakuphotomail.activity.GallerySendingMail.references";

	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
	private static final String EXTRA_ADDRESS_FROM = "addressFrom";
	private static final String EXTRA_ADDRESS_FROM_NAME = "addressFromName";
	private static final String EXTRA_ADDRESS_TO = "addressTo";
	private static final String EXTRA_ADDRESS_TO_NAME = "addressToName";
	private static final String EXTRA_MESSAGE_ID = "messageId";

	private Listener mListener = new Listener();

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.d("steinsgate", "GallerySendingMail#onCreate");
		setContentView(R.layout.gallery_sending_mail);
		setupViews();

		final Intent intent = getIntent();
		initInfo(intent);

		setMToAddressVisibility();

		MessagingController.getInstance(getApplication())
				.addListener(mListener);

	}

	private void setupViews() {
		Log.d("steinsgate", "GallerySendingMail#setupViews");
		mMessage = (EditText) findViewById(R.id.gallery_sending_mail_content);
		mTo = (TextView) findViewById(R.id.gallery_sending_mail_to_address);
		mToName = (TextView) findViewById(R.id.gallery_sending_mail_to_name);
		mSend = (Button) findViewById(R.id.gallery_sending_mail_send);
		mSend.setOnClickListener(this);
	}

	private void initInfo(Intent intent) {
		Log.d("steinsgate", "GallerySendingMail#initInfo");

		mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);

		final String accountUuid = (mMessageReference != null) ? mMessageReference.accountUuid
				: intent.getStringExtra(EXTRA_ACCOUNT);
		mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
		if (mAccount == null) {
			mAccount = Preferences.getPreferences(this).getDefaultAccount();
		}
		Log.d("steinsgate", "GallerySendingMail#initInfo accountUuid:"
				+ accountUuid);

		setMToAddress(intent.getStringExtra(EXTRA_ADDRESS_TO));
		setMToAddressName(intent.getStringExtra(EXTRA_ADDRESS_TO_NAME));
		mToAddress = new Address(mTo.getText().toString(), mToName.getText()
				.toString());

		setMFromAddress(intent.getStringExtra(EXTRA_ADDRESS_FROM),
				intent.getStringExtra(EXTRA_ADDRESS_FROM_NAME));

		mInReplyTo = intent.getStringExtra(EXTRA_MESSAGE_ID);
		mReferences = intent.getStringExtra(EXTRA_MESSAGE_ID);
	}

	private void setMToAddressVisibility() {
		Log.d("steinsgate", "GallerySendingMail#setMToAddressVisibility");
		int mToAddressNameVisibility = mToName == null
				|| "".equals(mToName.toString()) ? View.GONE : View.VISIBLE;
		mToName.setVisibility(mToAddressNameVisibility);
		if (mToAddressNameVisibility == View.VISIBLE) {
			mTo.setVisibility(View.GONE);
		}
	}

	private void setMToAddress(String addressTo) {
		Log.d("steinsgate", "GallerySendingMail#setMToAddress addressTo:"
				+ addressTo);
		mTo.setText(addressTo);
	}

	private void setMToAddressName(String addressToName) {
		Log.d("steinsgate",
				"GallerySendingMail#setMToAddressName addressToName:"
						+ addressToName);
		mToName.setText(addressToName);
	}

	private void setMFromAddress(String address, String personal) {
		Log.d("steinsgate", "GallerySendingMail#setMFromAddress address:"
				+ address + " personal:" + personal);
		mFromAddress = new Address(address, personal);
	}

	// FIXME i feel that the lack of entries.
	/**
	 * The framework handles most of the fields, but we need to handle stuff
	 * that we dynamically show and hide: Attachment list, Cc field, Bcc field,
	 * Quoted text,
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d("steinsgate", "GallerySendingMail#onSaveInstanceState");
		outState.putString(STATE_IN_REPLY_TO, mInReplyTo);
		outState.putString(STATE_REFERENCES, mReferences);

	}

	// FIXME i feel that the lack of entries.
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d("steinsgate", "GallerySendingMail#onRestoreInstanceState");
		mInReplyTo = savedInstanceState.getString(STATE_IN_REPLY_TO);
		mReferences = savedInstanceState.getString(STATE_REFERENCES);

	}

	@Override
	public void onClick(View v) {
		Log.d("steinsgate", "GallerySendingMail#onClick");
		if (onCheck()) {
			onSend();
			finish();
		}
	}

	private boolean onCheck() {
		Log.d("steinsgate", "GallerySendingMail#onCheck");
		String message = mMessage.getText().toString();
		if (17 < message.length()) {
			// XXX これで再入力させる展開になっているはず
			Log.d("steinsgate",
					"GallerySendingMail#onCheck message is to long :" + message);
			Toast.makeText(this, "メッセージが「" + message
					+ "」長すぎます。入力可能な文字数は17文字までです。", 1000);
			return false;
		}
		return true;
	}

	private void onSend() {
		Log.d("steinsgate", "GallerySendingMail#onSend");
		sendMessage();
	}

	private void sendMessage() {
		Log.d("steinsgate", "GallerySendingMail#sendMessage");
		new SendMessageTask().execute();
	}

	private class SendMessageTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Log.d("steinsgate",
					"GallerySendingMail SendMessageTask#doInBackground");
			/*
			 * Create the message from all the data the user has entered.
			 */
			MimeMessage message;
			try {
				message = createMessage();
			} catch (MessagingException me) {
				Log.e(RakuPhotoMail.LOG_TAG,
						"Failed to create new message for send or save.", me);
				throw new RuntimeException(
						"Failed to create a new message for send or save.", me);
			}

			MessagingController.getInstance(getApplication()).sendMessage(
					mAccount, message, null);

			return null;
		}
	}

	/**
	 * Build the message to be sent.
	 * 
	 * @return Message to be sent.
	 * @throws MessagingException
	 */
	private MimeMessage createMessage() throws MessagingException {
		Log.d("steinsgate", "GallerySendingMail#createMessage");

		MimeMessage message = new MimeMessage();
		// date
		message.addSentDate(new Date());
		// XXX Address From use mFromAddress
		message.setFrom(mFromAddress);
		Log.d("steinsgate", "GallerySendingMail#createMessage mFromAddress:"
				+ mFromAddress.getAddress() + ":" + mFromAddress.getPersonal());
		// XXX Address To use mToAddress & mToAddressName
		Log.d("steinsgate", "GallerySendingMail#createMessage mToAddress:"
				+ mToAddress.getAddress() + ":" + mToAddress.getPersonal());
		message.setRecipient(RecipientType.TO, mToAddress);
		// subject
		message.setSubject(mMessage.getText().toString());
		Log.d("steinsgate", "GallerySendingMail#createMessage mMessage:"
				+ mMessage.getText().toString());
		// header
		message.setHeader("User-Agent", getString(R.string.message_header_mua));
		// reply to
		// FIXME Reply specifying targets for case
		if (mInReplyTo != null) {
			message.setInReplyTo(mInReplyTo);
		}
		Log.d("steinsgate", "GallerySendingMail#createMessage mInReplyTo:"
				+ mInReplyTo);
		// references
		// FIXME Reply specifying targets for case
		if (mReferences != null) {
			message.setReferences(mReferences);
		}
		Log.d("steinsgate", "GallerySendingMail#createMessage mReferences:"
				+ mReferences);
		// body
		TextBody body = null;
		body = new TextBody("このメールは、らくフォトメールの試験用メールです。");
		message.setBody(body);

		return message;
	}

	/**
	 * Compose a new message as a reply to the given message. If replyAll is
	 * true the function is reply all instead of simply reply.
	 * 
	 * @param context
	 * @param message
	 * @param replyAll
	 * @param messageBody
	 *            optional, for decrypted messages, null if it should be grabbed
	 *            from the given message
	 */
	public static void actionReply(Context context, MessageReference reference,
			MessageBean messageBean) {
		Log.d("steinsgate", "GallerySendingMail#actionReply");
		Intent i = new Intent(context, GallerySendingMail.class);
		// i.putExtra(EXTRA_MESSAGE_UID, messageBean.getUid());
		i.putExtra(EXTRA_ADDRESS_TO, messageBean.getSenderList());
		i.putExtra(EXTRA_ADDRESS_TO_NAME, messageBean.getSenderListName());
		i.putExtra(EXTRA_ADDRESS_FROM, messageBean.getToList());
		i.putExtra(EXTRA_ADDRESS_FROM_NAME, messageBean.getToListName());
		i.putExtra(EXTRA_MESSAGE_ID, messageBean.getMessageId());
		i.putExtra(EXTRA_MESSAGE_REFERENCE, reference);
		context.startActivity(i);
	}

	class Listener extends MessagingListener {
		@Override
		public void loadMessageForViewStarted(Account account, String folder,
				String uid) {
			Log.d("steinsgate",
					"GallerySendingMail Listener#loadMessageForViewStarted");

			if ((mMessageReference == null)
					|| !mMessageReference.uid.equals(uid)) {
				return;
			}
		}

		@Override
		public void loadMessageForViewFinished(Account account, String folder,
				String uid, Message message) {
			Log.d("steinsgate",
					"GallerySendingMail Listener#loadMessageForViewFinished");

			if ((mMessageReference == null)
					|| !mMessageReference.uid.equals(uid)) {
				return;
			}
		}

		@Override
		public void loadMessageForViewBodyAvailable(Account account,
				String folder, String uid, final Message message) {
			Log.d("steinsgate",
					"GallerySendingMail Listener#loadMessageForViewBodyAvailable");

			if ((mMessageReference == null)
					|| !mMessageReference.uid.equals(uid)) {
				return;
			}
			try {
				if (message.getMessageId() != null
						&& message.getMessageId().length() > 0) {
					mInReplyTo = message.getMessageId();

					if (message.getReferences() != null
							&& message.getReferences().length > 0) {
						StringBuffer buffy = new StringBuffer();
						for (int i = 0; i < message.getReferences().length; i++)
							buffy.append(message.getReferences()[i]);

						mReferences = buffy.toString() + " " + mInReplyTo;
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
