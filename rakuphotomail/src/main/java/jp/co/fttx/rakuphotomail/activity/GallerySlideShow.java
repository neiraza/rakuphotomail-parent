/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.*;
import android.os.IBinder;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.mail.FetchProfile;
import jp.co.fttx.rakuphotomail.mail.Flag;
import jp.co.fttx.rakuphotomail.mail.Folder;
import jp.co.fttx.rakuphotomail.mail.Folder.OpenMode;
import jp.co.fttx.rakuphotomail.mail.Message;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Multipart;
import jp.co.fttx.rakuphotomail.mail.Part;
import jp.co.fttx.rakuphotomail.mail.Store;
import jp.co.fttx.rakuphotomail.mail.internet.MimeUtility;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.Attachments;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalAttachmentBodyPart;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalFolder;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalMessage;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.MessageInfo;
import jp.co.fttx.rakuphotomail.mail.store.UnavailableStorageException;
import jp.co.fttx.rakuphotomail.provider.AttachmentProvider;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;
import jp.co.fttx.rakuphotomail.rakuraku.util.Rotate3dAnimation;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqReceiver;
import jp.co.fttx.rakuphotomail.service.AttachmentSynqService;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideShow extends RakuPhotoActivity implements
		View.OnClickListener, OnItemClickListener {

	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_FOLDER = "folder";

	private Account mAccount;
	private String mFolderName;
	private MessageBean messageBean;
	private AttachmentBean attachmentBean;
	private MessageBean newMessageBean;
	private CopyOnWriteArrayList<AttachmentBean> newAttachmentList;
	private volatile String mMessageUid = null;

	/*
	 * Slide
	 */
	private TextView mSubject;
	private ViewGroup mContainer;
	private ImageView mImageViewDefault;
	private ImageView mImageViewEven;
	private ImageView mImageViewOdd;
	/*
	 * new mail(detail mail)
	 */
	private ImageView mImageViewPicture;
	private TextView mMailSubject;
	private TextView mMailDate;
	private TextView mAnswered;
	private TextView mMailPre;
	private TextView mMailSeparator1;
	private TextView mMailSlide;
	private TextView mMailNext;
	private TextView mMailSeparator3;
	private TextView mMailReply;
	private ImageAdapter mImageAdapter;
	private LinearLayout mGalleryLinearLayout;
	private Gallery mGallery;

	/*
	 * anime
	 */
	private float centerX;
	private float centerY;
	private int DURATION = 500;

	private Thread createMessageListThread;
	private Runnable createMessageList;
	private volatile boolean isSlideRepeat = true;
	private volatile boolean isCheckRepeat = true;
	private Thread messageSlideThread;
	private Runnable messageSlide;
	private Handler handler;
	private Runnable setMailInfo;
	private Runnable setNewMailInfo;
	private Runnable checkMail;
	private Runnable setMailInit;
	private Thread checkMailThread;
	private Thread restartMesasgeSlideThread;
	private Thread restartCheckMailThread;
	private Context mContext;

	private CopyOnWriteArrayList<String> mMessageUids = new CopyOnWriteArrayList<String>();
	private ConcurrentHashMap<String, MessageBean> mMessages = new ConcurrentHashMap<String, MessageBean>();
	private MessageBean mMessageInit = new MessageBean();
	private AttachmentBean mAttachmentInit = new AttachmentBean();
	private volatile long mDispAttachmentId;
	private volatile long mDispMessageId;

	private AttachmentSynqService synqService;
	private boolean mIsBound = false;
	private AttachmentSynqReceiver receiver = new AttachmentSynqReceiver();

	public static void actionHandleFolder(Context context, Account account,
			String folder) {
		Intent intent = actionHandleFolderIntent(context, account, folder);
		context.startActivity(intent);
	}

	public static Intent actionHandleFolderIntent(Context context,
			Account account, String folder) {
		Intent intent = new Intent(context, GallerySlideShow.class);
		if (account != null) {
			intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
		}
		if (folder != null) {
			intent.putExtra(EXTRA_FOLDER, folder);
		}
		return intent;
	}

	private void doBindService() {
		Log.d("download_test", "GallerySlideShow#doBindService start");
		if (!mIsBound) {
			Log.d("download_test", "GallerySlideShow#doBindService mIsBound:"
					+ mIsBound);
			mIsBound = bindService(getIntent(), mConnection,
					Context.BIND_AUTO_CREATE);
			IntentFilter filter = new IntentFilter(AttachmentSynqService.ACTION);
			registerReceiver(receiver, filter);
		}
		Log.d("download_test", "GallerySlideShow#doBindService end");
	}

	private void doUnbindService() {
		Log.d("download_test", "GallerySlideShow#doUnbindService start");
		if (mIsBound) {
			Log.d("hoge", "AttachmentSynqTestActivity#doUnBind unbindService");
			unbindService(mConnection);
			mIsBound = false;
			unregisterReceiver(receiver);
		}
		Log.d("download_test", "GallerySlideShow#doUnbindService end");
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("download_test", "ServiceConnection#onServiceConnected");
			synqService = ((AttachmentSynqService.AttachmentSynqBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("download_test", "ServiceConnection#onServiceDisconnected");
			synqService = null;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("download_test", "GallerySlideShow#onCreate start");
		Log.d("neko", "onCreate start");
		super.onCreate(savedInstanceState);
		mContext = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gallery_slide_show);
		setupViews();
		onNewIntent(getIntent());
		doBindService();
		initThreading();
		Log.d("neko", "onCreate end");
		Log.d("download_test", "GallerySlideShow#onCreate end");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("download_test", "GallerySlideShow#onSaveInstanceState start");
		outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
		outState.putString(EXTRA_FOLDER, mFolderName);
		Log.d("download_test", "GallerySlideShow#onSaveInstanceState end");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d("download_test", "GallerySlideShow#onRestoreInstanceState start");
		super.onRestoreInstanceState(savedInstanceState);
		mAccount = Preferences.getPreferences(this).getAccount(
				savedInstanceState.getString(EXTRA_ACCOUNT));
		mFolderName = savedInstanceState.getString(EXTRA_FOLDER);
		Log.d("download_test", "GallerySlideShow#onRestoreInstanceState end");
	}

	private void setupViews() {
		mSubject = (TextView) findViewById(R.id.gallery_subject);
		mContainer = (ViewGroup) findViewById(R.id.gallery_container);
		mImageViewDefault = (ImageView) findViewById(R.id.gallery_attachment_picuture_default);
		// mImageViewDefault.setVisibility(View.VISIBLE);
		mImageViewDefault.setVisibility(View.GONE);
		mImageViewEven = (ImageView) findViewById(R.id.gallery_attachment_picuture_even);
		mImageViewEven.setOnClickListener(this);
		mImageViewEven.setVisibility(View.GONE);
		mImageViewOdd = (ImageView) findViewById(R.id.gallery_attachment_picuture_odd);
		mImageViewOdd.setOnClickListener(this);
		mImageViewOdd.setVisibility(View.GONE);
	}

	private void setupViewsMailDetail() {
		/*
		 * 新着メール表示（詳細メール表示）
		 */
		mGalleryLinearLayout = (LinearLayout) findViewById(R.id.gallery_mail_picture_slide_linear_layoput);
		mImageViewPicture = (ImageView) findViewById(R.id.gallery_mail_picuture);
		mImageViewPicture.setVisibility(View.VISIBLE);
		mMailSubject = (TextView) findViewById(R.id.gallery_mail_subject);
		mMailDate = (TextView) findViewById(R.id.gallery_mail_date);
		mAnswered = (TextView) findViewById(R.id.gallery_mail_sent_flag);
		mAnswered.setVisibility(View.GONE);
		mMailPre = (TextView) findViewById(R.id.gallery_mail_pre);
		mMailPre.setOnClickListener(this);
		mMailSeparator1 = (TextView) findViewById(R.id.gallery_mail_separator1);
		mMailSlide = (TextView) findViewById(R.id.gallery_mail_slide);
		mMailSlide.setOnClickListener(this);
		mMailReply = (TextView) findViewById(R.id.gallery_mail_reply);
		mMailReply.setOnClickListener(this);
		mMailSeparator3 = (TextView) findViewById(R.id.gallery_mail_separator3);
		mMailNext = (TextView) findViewById(R.id.gallery_mail_next);
		mMailNext.setOnClickListener(this);
		mGallery = (Gallery) findViewById(R.id.gallery_mail_picture_slide);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.d("download_test", "GallerySlideShow#onNewIntent start");
		setIntent(intent);
		mAccount = Preferences.getPreferences(this).getAccount(
				intent.getStringExtra(EXTRA_ACCOUNT));
		mFolderName = intent.getStringExtra(EXTRA_FOLDER);
		Log.d("download_test", "GallerySlideShow#onNewIntent mFolderName:"
				+ mFolderName);
		// mController = MessagingController.getInstance(getApplication());
		Log.d("download_test", "GallerySlideShow#onNewIntent end");
	}

	@Override
	public void onResume() {
		Log.d("neko", "onResume start");
		super.onResume();
		createMessageListThread.start();
		checkMailThread.start();
		Log.d("neko", "onResume end");
	}

	private void setMailInit(MessageBean message, AttachmentBean attachment) {
		Bitmap bitmap = populateFromPart(attachment.getPart());
		if (bitmap == null) {
			return;
		}
		mImageViewEven.setVisibility(View.VISIBLE);
		mImageViewEven.setImageBitmap(bitmap);
		mSubject.setText(message.getSubject());
	}

	private void setMailEffect(MessageBean message, AttachmentBean attachment) {
		Bitmap bitmap = populateFromPart(attachment.getPart());
		if (bitmap == null) {
			return;
		}
		// TODO ここは後々に外部からインジェクションして、エフェクト変更する仕組みにかえいたい
		applyRotation(bitmap);
		// XXX この位置を初回時しか表示されないように工夫したい
		// mImageViewDefault.setVisibility(View.GONE);
		mSubject.setText(message.getSubject());
	}

	private void initThreading() {
		Log.d("neko", "initThreading start");
		handler = new Handler();
		setMailInfo = new Runnable() {
			@Override
			public void run() {
				Log.d("neko", "setMailInfo run() start");
				setMailEffect(messageBean, attachmentBean);
				Log.d("neko", "setMailInfo  run() end");
			}
		};

		setMailInit = new Runnable() {
			@Override
			public void run() {
				Log.d("neko", "setMailInit  run() start");
				setMailInit(mMessageInit, mAttachmentInit);
				Log.d("neko", "setMailInit  run() end");
			}
		};

		// XXX setMailDisp(int index)の一部と共通化できそう
		setNewMailInfo = new Runnable() {
			@Override
			public void run() {
				Log.d("neko", "setNewMailInfo  run() start");
				setContentView(R.layout.gallery_slide_show_stop);
				setupViewsMailDetail();
				Bitmap bitmap = populateFromPart(newAttachmentList.get(0)
						.getPart());
				if (bitmap == null) {
					return;
				}
				mImageViewPicture.setImageBitmap(bitmap);
				if (newAttachmentList.size() > 1) {
					ArrayList<Bitmap> list = new ArrayList<Bitmap>();
					for (AttachmentBean bean : newAttachmentList) {
						list.add(populateFromPartThumbnail(bean.getPart()));
					}
					mImageAdapter = new ImageAdapter(mContext);
					mImageAdapter.setImageItems(list);
					mGallery.setAdapter(mImageAdapter);
					mGallery.setOnItemClickListener((OnItemClickListener) mContext);
				} else {
					mGalleryLinearLayout.setVisibility(View.GONE);
				}
				// XXX mMessageUidsにnewMessageBean.getUid()を追加してしまおう
				// String uid = newMessageBean.getUid();
				mMessageUid = newMessageBean.getUid();
				mMessageUids.add(0, mMessageUid);
				// FIXME mMessage.putしる!!!!!!
				mMessages.put(mMessageUid, newMessageBean);
				// mMessageUid = uid;
				if (0 >= mMessageUids.indexOf(mMessageUid)) {
					mMailNext.setVisibility(View.GONE);
					mMailSeparator3.setVisibility(View.GONE);
				}
				setupViewMail(newMessageBean);
				Log.d("neko", "setNewMailInfo  run() end");
			}
		};

		createMessageList = new Runnable() {
			@Override
			public void run() {
				Log.d("neko", "createMessageList  run() start");
				List<MessageInfo> messageInfoList = getMessages();
				if (messageInfoList.size() > 0) {
					try {
						setSlideInfo(messageInfoList);
					} catch (MessagingException e) {
						e.printStackTrace();
						Log.e("rakuphotomail", "Happy!Happy!Error!", e);
					}
					messageSlideThread.start();
				} else {
					onStop();
				}
				Log.d("neko", "createMessageList  run() end");
			}
		};
		createMessageListThread = new Thread(createMessageList);

		messageSlide = new Runnable() {
			@Override
			public void run() {
				Log.d("neko", "messageSlide  run() start");
				// XXX init disp !!!!!!!!!!!!
				checkMessageUid(mMessageUid);
				// XXX ここに初期表示処理を置く、初期表示に使用したIDを保持する
				mMessageInit = mMessages.get(mMessageUid);
				mAttachmentInit = mMessageInit.getAttachments().get(0);
				handler.post(setMailInit);
				mDispMessageId = mMessageInit.getId();
				mDispAttachmentId = mAttachmentInit.getId();
				while (isSlideRepeat) {
					slideShowStart();
				}
				Log.d("neko", "messageSlide  run() end");
			}
		};
		messageSlideThread = new Thread(messageSlide);

		checkMail = new Runnable() {
			@Override
			public void run() {
				Log.d("neko", "checkMail  run() start");
				ArrayList<MessageBean> messages = null;
				while (isCheckRepeat) {
					sleep(30000);
					synchronizeMailbox(mAccount, mFolderName);
					messages = getNewMail();
					if (messages != null && messages.size() > 0) {
						repeatEnd();
					}
				}
				if (messages != null && messages.size() > 0) {
					slideShowNewMailStart(messages);
				}
				Log.d("neko", "checkMail  run() end");
			}
		};
		checkMailThread = new Thread(checkMail);
		Log.d("neko", "initThreading end");
	}

	private ArrayList<MessageBean> getNewMail() {
		List<MessageInfo> newMessages = getMessages();
		ArrayList<MessageBean> messages = null;
		if (newMessages.size() > 0) {
			messages = new ArrayList<MessageBean>();
			for (MessageInfo newMessage : newMessages) {
				String uid = newMessage.getUid();
				if (!mMessageUids.contains(uid)) {
					LocalMessage message = loadMessage(mAccount, mFolderName,
							uid);
					MessageBean mb = setMessage(message, newMessage);
					if (mb.getAttachmentCount() > 0) {
						CopyOnWriteArrayList<AttachmentBean> attachments = renderAttachmentsNewMail(message);
						if (attachments != null && attachments.size() > 0) {
							mb.setAttachments(attachments);
							messages.add(mb);
						}
					}
				}
			}
		}
		return messages;
	}

	private void slideShowNewMailStart(ArrayList<MessageBean> messages) {
		// TODO 初回は新着メールが複数ない場合で実現してみる
		newMessageBean = messages.get(0);
		newAttachmentList = newMessageBean.getAttachments();
		handler.post(setNewMailInfo);
	}

	private int checkMessageUid(String messageUid) {
		int index = mMessageUids.indexOf(messageUid);
		if (index == -1) {
			index = 0;
			this.mMessageUid = mMessageUids.get(index);
		}
		return index;
	}

	// XXX ここがガンだ。特に初期表示時にデフォルトを一回経由して表示するのは悪しき習慣
	private void slideShowStart() {
		Log.d("neko", "slideShowStart start");
		if (mMessageUids == null || mMessageUids.size() == 0) {
			onStop();
		}
		int index = checkMessageUid(mMessageUid);
		for (; index < mMessageUids.size(); index++) {
			if (isSlideRepeat) {
				mMessageUid = mMessageUids.get(index);
				messageBean = mMessages.get(mMessageUid);
				if (messageBean != null) {
					CopyOnWriteArrayList<AttachmentBean> attachments = messageBean
							.getAttachments();
					// XXX 最後に表示したメッセージIDを引数に追加する
					mDispAttachmentId = slideShowAttachmentLoop(attachments,
							mDispMessageId, mDispAttachmentId);
					mDispMessageId = messageBean.getId();
					Log.d("neko", "slideShowStart isSlideRepeat:"
							+ isSlideRepeat);
					Log.d("neko", "slideShowStart mDispMessageId:"
							+ mDispMessageId + " mDispAttachmentId:"
							+ mDispAttachmentId);
				} else {
					onStop();
				}
			} else {
				return;
			}
		}
		if (isSlideRepeat) {
			mMessageUid = mMessageUids.get(0);
		}
		Log.d("neko", "slideShowStart end");
	}

	private long slideShowAttachmentLoop(
			CopyOnWriteArrayList<AttachmentBean> attachments,
			long dispMessageId, long dispAttachmentId) {
		long result = 0;
		for (AttachmentBean attachment : attachments) {
			// XXX ここでメッセージIDとアタッチメントIDが同一の場合は切り替え処理をスルーして次にいく
			// 前スライドと同一データ時は切り替えない
			if (messageBean.getId() != dispMessageId
					&& attachment.getId() != dispAttachmentId) {
				// if (!isSlideRepeat) {
				// }
				attachmentBean = attachment;
				handler.post(setMailInfo);
				result = attachment.getId();
			}
			// XXX ここは不変
			sleep(10000);
		}
		return result;
	}

	private List<MessageInfo> getMessages() {
		Log.d("download_test", "GallerySlideShow#getMessages start");
		LocalStore localStore = null;
		LocalFolder localFolder = null;
		try {
			localStore = mAccount.getLocalStore();
			Log.d("download_test", "GallerySlideShow#getMessages mFolderName:"
					+ mFolderName);
			localFolder = localStore.getFolder(mFolderName);
			localFolder.open(OpenMode.READ_WRITE);
			return localStore.getMessages(localFolder.getId());
		} catch (MessagingException e) {
			e.printStackTrace();
			closeFolder(localFolder);
		}
		Log.d("download_test", "GallerySlideShow#getMessages end");
		return null;
	}

	private void setSlideInfo(List<MessageInfo> messageInfoList)
			throws MessagingException {
		Log.d("neko",
				"setSlideInfo start mMessageUids.size():" + mMessageUids.size());
		mMessages.clear();
		mMessageUids.clear();
		for (MessageInfo messageInfo : messageInfoList) {
			String uid = messageInfo.getUid();
			LocalMessage message = loadMessage(mAccount, mFolderName, uid);
			if (message == null) {
				onDestroy();
			}
			if (message.getAttachmentCount() > 0) {
				// XXX loadAttachmentを実装する際に使えるかも
				// mMessage = message;
				MessageBean mb = setMessage(message, messageInfo);
				CopyOnWriteArrayList<AttachmentBean> attachments = renderAttachments(
						message, message.getUid());
				if (attachments.size() > 0) {
					mb.setAttachments(attachments);
					mMessages.put(String.valueOf(uid), mb);
					mMessageUids.add(uid);
				}
			}
		}
		Log.d("neko",
				"setSlideInfo end mMessageUids.size():" + mMessageUids.size());
	}

	private MessageBean setMessage(LocalMessage message, MessageInfo messageInfo) {
		MessageBean messageBean = new MessageBean();
		messageBean.setId(message.getId());
		messageBean.setSubject(message.getSubject());
		messageBean.setUid(message.getUid());
		messageBean.setAttachmentCount(message.getAttachmentCount());
		messageBean.setDate(messageInfo.getDate());
		messageBean.setTextContent(messageInfo.getTextContent());
		messageBean.setSenderList(messageInfo.getSenderList());
		String[] mailFromArr = messageInfo.getSenderList().split(";");
		if (mailFromArr == null || mailFromArr.length == 0) {
		} else if (mailFromArr.length == 1) {
			messageBean.setSenderAddress(mailFromArr[0]);
		} else {
			messageBean.setSenderAddress(mailFromArr[0]);
			messageBean.setSenderName(mailFromArr[1]);
		}
		messageBean.setToList(messageInfo.getToList());
		messageBean.setCcList(messageInfo.getCcList());
		messageBean.setBccList(messageInfo.getBccList());
		messageBean.setMessageId(messageInfo.getMessageId());
		messageBean.setMessage(message);
		// TODO 他にもあるかも
		// [X_GOT_ALL_HEADERS,X_DOWNLOADED_FULL,SEEN,ANSWERED,X_DOWNLOADED_PARTIAL,X_REMOTE_COPY_STARTED]
		String flags = messageInfo.getFlags();
		messageBean.setFlags(flags);
		String[] flagList = RakuPhotoStringUtils.splitFlags(flags);
		if (null != flagList && flagList.length != 0) {
			setFlag(flagList, messageBean);
		}
		return messageBean;
	}

	/**
	 * setFlag.
	 * <ul>
	 * <li>X_GOT_ALL_HEADERS</li>
	 * <li>X_DOWNLOADED_FULL</li>
	 * <li>SEEN</li>
	 * <li>ANSWERED</li>
	 * <li>X_DOWNLOADED_PARTIAL</li>
	 * <li>X_REMOTE_COPY_STARTED</li>
	 * </ul>
	 * 
	 * @param flag
	 */
	private void setFlag(String[] flag, MessageBean messageBean) {
		StringBuilder builder = new StringBuilder();
		for (String f : flag) {
			if ("X_GOT_ALL_HEADERS".equals(f)) {
				messageBean.setFlagXGotAllHeaders(true);
			} else if ("SEEN".equals(f)) {
				messageBean.setFlagSeen(true);
			} else if ("ANSWERED".equals(f)) {
				messageBean.setFlagAnswered(true);
			} else if ("X_DOWNLOADED_FULL".equals(f)) {
				messageBean.setFlagXDownLoadedFull(true);
			} else if ("X_DOWNLOADED_PARTIAL".equals(f)) {
				messageBean.setFlagXDownLoadedPartial(true);
			} else if ("X_REMOTE_COPY_STARTED".equals(f)) {
				messageBean.setFlagXRemoteCopyStarted(true);
			} else {
				builder.append(f + ",");
			}
			int len = builder.length();
			if (0 != len) {
				messageBean.setFlagOther(builder.delete(len - 1, len)
						.toString());
			}
		}
	}

	/**
	 * loadMessage. Mail to get the BodyPart are not stored in the SQLite.
	 * 
	 * @param account
	 * @param folder
	 * @param uid
	 * @return
	 * @author tooru.oguri
	 */
	private LocalMessage loadMessage(final Account account,
			final String folder, final String uid) {
		try {
			LocalStore localStore = account.getLocalStore();
			LocalFolder localFolder = localStore.getFolder(folder);
			localFolder.open(OpenMode.READ_WRITE);
			LocalMessage message = (LocalMessage) localFolder.getMessage(uid);

			FetchProfile fp = new FetchProfile();
			fp.add(FetchProfile.Item.ENVELOPE);
			fp.add(FetchProfile.Item.BODY);
			localFolder.fetch(new Message[] { message }, fp, null);
			localFolder.close();
			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public CopyOnWriteArrayList<AttachmentBean> renderAttachments(Part part,
			String uid) throws MessagingException {
		CopyOnWriteArrayList<AttachmentBean> attachments = null;
		if (part.getBody() instanceof Multipart) {
			try {
				attachments = splitMultipart(part, uid);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} else if (part instanceof LocalStore.LocalAttachmentBodyPart) {
			attachments = new CopyOnWriteArrayList<AttachmentBean>();
			AttachmentBean attachment = setAttachment(part);
			if (isSlide(attachment)) {
				attachments.add(attachment);
				synqService.onDownload(mAccount, mFolderName, uid);
			}
		} else {
			return new CopyOnWriteArrayList<AttachmentBean>();
		}
		return attachments;
	}

	public CopyOnWriteArrayList<AttachmentBean> renderAttachmentsNewMail(
			Part part) {
		CopyOnWriteArrayList<AttachmentBean> attachments = null;
		if (part.getBody() instanceof Multipart) {
			try {
				attachments = splitMultipart(part, null);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} else if (part instanceof LocalStore.LocalAttachmentBodyPart) {
			attachments = new CopyOnWriteArrayList<AttachmentBean>();
			AttachmentBean attachment = setAttachment(part);
			if (isSlide(attachment)) {
				attachments.add(attachment);
			}
		}
		return attachments;
	}

	private CopyOnWriteArrayList<AttachmentBean> splitMultipart(Part part,
			String uid) throws MessagingException {
		Multipart mp = (Multipart) part.getBody();
		CopyOnWriteArrayList<AttachmentBean> attachments = new CopyOnWriteArrayList<AttachmentBean>();
		for (int i = 0; i < mp.getCount(); i++) {
			if (mp.getBodyPart(i) instanceof LocalStore.LocalAttachmentBodyPart) {
				AttachmentBean attachment = setAttachment(mp.getBodyPart(i));
				if (isSlide(attachment)) {
					attachments.add(setAttachment(mp.getBodyPart(i)));
					// XXX ここでuid炸裂！
					if (null != uid) {
						// FIXME nullpo!!!!!!!!!!!!!!!!!!!!!!!!!
						Log.d("download_test",
								"GallerySlideShow#splitMultipart synqService:"
										+ synqService);
						synqService.onDownload(mAccount, mFolderName, uid);
					}
				}
			}
		}
		return attachments;
	}

	private AttachmentBean setAttachment(Part part) {
		AttachmentBean attachment = new AttachmentBean();
		String contentDisposition = null;
		try {
			contentDisposition = MimeUtility.unfoldAndDecode(part
					.getDisposition());
			if (contentDisposition != null
					&& MimeUtility.getHeaderParameter(contentDisposition, null)
							.matches("^(?i:inline)")
					&& part.getHeader("Content-ID") != null) {
				return null;
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		LocalAttachmentBodyPart labPart = (LocalAttachmentBodyPart) part;
		long attachmentId = labPart.getAttachmentId();
		attachment.setId(attachmentId);
		Attachments attachments = getAttachment(attachmentId);
		attachment.setMimeType(attachments.getMimeType());
		attachment.setName(attachments.getName());
		attachment.setSize(Integer.valueOf(attachments.getSize()));
		attachment.setPart(labPart);
		return attachment;
	}

	private Attachments getAttachment(long attachmentId) {
		LocalStore localStore = null;
		try {
			localStore = mAccount.getLocalStore();
			return localStore.getAttachment(attachmentId);
		} catch (UnavailableStorageException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void closeFolder(Folder f) {
		if (f != null) {
			f.close();
		}
	}

	private Bitmap populateFromPart(LocalAttachmentBodyPart part) {
		Bitmap bitmapView = null;
		try {
			bitmapView = getBitmapView(part);
			// XXX loadAttachmentが必要になったら実装する
			// if (bitmapView == null) {
			// Log.d("steinsgate", "GallerySlideShow#populateFromPart 画像dない");
			// loadAttachment(mAccount, mMessage, part);
			// }
		} catch (Exception e) {
			Log.e(RakuPhotoMail.LOG_TAG, "error ", e);
			e.printStackTrace();
		}
		return bitmapView;
	}

	private Bitmap populateFromPartThumbnail(LocalAttachmentBodyPart part) {
		Bitmap bitmapView = null;
		try {
			bitmapView = getThumbnailBitmapView(part);
			// XXX loadAttachmentが必要になったら実装する
			// if (bitmapView == null) {
			// Log.d("steinsgate", "GallerySlideShow#populateFromPart 画像ない");
			// loadAttachment(mAccount, mMessage, part);
			// }
		} catch (Exception e) {
			Log.e(RakuPhotoMail.LOG_TAG, "error ", e);
			e.printStackTrace();
		}
		return bitmapView;
	}

	private Bitmap getBitmapView(LocalAttachmentBodyPart part) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			Uri uri = AttachmentProvider.getAttachmentUri(mAccount,
					part.getAttachmentId());
			options.inJustDecodeBounds = true;
			this.getContentResolver().openInputStream(uri);
			BitmapFactory.decodeStream(this.getContentResolver()
					.openInputStream(uri), null, options);
			int displayW = getWindowManager().getDefaultDisplay().getWidth();
			int displayH = getWindowManager().getDefaultDisplay().getHeight();
			int scaleW = options.outWidth / displayW + 1;
			int scaleH = options.outHeight / displayH + 1;
			options.inJustDecodeBounds = false;
			options.inSampleSize = Math.max(scaleW, scaleH);
			return BitmapFactory.decodeStream(this.getContentResolver()
					.openInputStream(uri), null, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Bitmap getThumbnailBitmapView(LocalAttachmentBodyPart part) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			Uri uri = AttachmentProvider.getAttachmentUri(mAccount,
					part.getAttachmentId());
			options.inJustDecodeBounds = true;
			this.getContentResolver().openInputStream(uri);
			BitmapFactory.decodeStream(this.getContentResolver()
					.openInputStream(uri), null, options);
			// XXX 最終的にこのレイアウトサイズでOKOK？
			int displayW = 150;
			int displayH = 100;
			int scaleW = options.outWidth / displayW + 1;
			int scaleH = options.outHeight / displayH + 1;
			options.inJustDecodeBounds = false;
			options.inSampleSize = Math.max(scaleW, scaleH);
			return BitmapFactory.decodeStream(this.getContentResolver()
					.openInputStream(uri), null, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// XXX サービス化したいなぁ。必要になったら実装するってことで。
	// private void loadAttachment(final Account account, final Message message,
	// final Part part) {
	// Log.d("steinsgate", "GallerySlideShow#loadAttachment");
	// Folder remoteFolder = null;
	// LocalFolder localFolder = null;
	// try {
	// LocalStore localStore;
	// localStore = mAccount.getLocalStore();
	// ArrayList<Part> viewables = new ArrayList<Part>();
	// ArrayList<Part> attachments = new ArrayList<Part>();
	// MimeUtility.collectParts(message, viewables, attachments);
	// for (Part attachment : attachments) {
	// attachment.setBody(null);
	// }
	// Store remoteStore = account.getRemoteStore();
	// localFolder = localStore.getFolder(message.getFolder().getName());
	// remoteFolder = remoteStore.getFolder(message.getFolder().getName());
	// remoteFolder.open(OpenMode.READ_WRITE);
	// Message remoteMessage = remoteFolder.getMessage(message.getUid());
	// remoteMessage.setBody(message.getBody());
	// remoteFolder.fetchPart(remoteMessage, part, null);
	// localFolder.updateMessage((LocalMessage) message);
	// } catch (MessagingException e) {
	// e.printStackTrace();
	// closeFolder(localFolder);
	// closeFolder(remoteFolder);
	// }
	// }

	private boolean isSlide(AttachmentBean attachment) {
		String mimeType = attachment.getMimeType();
		String fileName = attachment.getName();
		return "image/jpeg".equals(mimeType)
				|| "image/png".equals(mimeType)
				|| (null != fileName && (fileName.endsWith(".png") || fileName
						.endsWith(".JPG")));
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		repeatEnd();
		// FIXME これをnullにするとメール返信画面から戻ってこれないお
		// createMessageListThread = null;
		// messageSlideThread = null;
		// checkMailThread = null;
		// XXX what?
		// finish();
	}

	@Override
	public void onDestroy() {
		Log.d("download_test", "GallerySlideShow#onDestroy start");
		super.onDestroy();
		repeatEnd();
		createMessageListThread = null;
		messageSlideThread = null;
		checkMailThread = null;
		doUnbindService();
		// finish();
		Log.d("download_test", "GallerySlideShow#onDestroy end");
	}

	private void repeatEnd() {
		isSlideRepeat = false;
		isCheckRepeat = false;
	}

	private void applyRotation(Bitmap bitmap) {
		if (mImageViewEven.getVisibility() == View.GONE) {
			mImageViewEven.setImageBitmap(bitmap);
			applyRotation(mContainer, 0f, 90f, 180f, 0f);
		} else {
			mImageViewOdd.setImageBitmap(bitmap);
			applyRotation(mContainer, 180f, 270f, 360f, 0f);
		}
	}

	private void applyRotation(ViewGroup view, float start, float mid,
			float end, float depth) {
		this.centerX = view.getWidth() / 2.0f;
		this.centerY = view.getHeight() / 2.0f;
		Rotate3dAnimation rot = new Rotate3dAnimation(start, mid, centerX,
				centerY, depth, true);
		rot.setDuration(DURATION);
		rot.setAnimationListener(new DisplayNextView(mid, end, depth));
		view.startAnimation(rot);
	}

	public class DisplayNextView implements AnimationListener {
		private float mid;
		private float end;
		private float depth;

		public DisplayNextView(float mid, float end, float depth) {
			this.mid = mid;
			this.end = end;
			this.depth = depth;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mContainer.post(new Runnable() {
				@Override
				public void run() {
					if (mImageViewEven.getVisibility() == View.GONE) {
						mImageViewEven.setVisibility(View.VISIBLE);
						mImageViewOdd.setVisibility(View.GONE);
					} else {
						mImageViewEven.setVisibility(View.GONE);
						mImageViewOdd.setVisibility(View.VISIBLE);
					}
					// XXX ここが変な反転させている所か？？？
					Rotate3dAnimation rot = new Rotate3dAnimation(mid, end,
							centerX, centerY, depth, false);
					rot.setDuration(DURATION);
					rot.setInterpolator(new AccelerateInterpolator());
					mContainer.startAnimation(rot);
				}
			});
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

	}

	private void setupViewMail(MessageBean message) {
		Log.d("neko", "setupViewMail start");
		mMailSubject.setText(message.getSubject());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd h:mm a");
		// XXX sdf.format(message.getDate())????
		mMailDate.setText(sdf.format(message.getDate()));
		// XXX あれ機能してる？
		if (message.isFlagAnswered()) {
			mAnswered.setVisibility(View.VISIBLE);
		}
		Log.d("neko", "setupViewMail end");
	}

	private void synchronizeMailbox(Account account, String folderName) {
		Folder remoteFolder = null;
		LocalFolder tLocalFolder = null;

		/*
		 * We don't ever sync the Outbox or errors folder
		 */
		if (folderName.equals(account.getOutboxFolderName())
				|| folderName.equals(account.getErrorFolderName())) {
			return;
		}

		try {

			/*
			 * Get the message list from the local store and create an index of
			 * the uids within the list.
			 */
			final LocalStore localStore = account.getLocalStore();
			tLocalFolder = localStore.getFolder(folderName);
			final LocalFolder localFolder = tLocalFolder;
			localFolder.open(OpenMode.READ_WRITE);
			localFolder.updateLastUid();
			Message[] localMessages = localFolder.getMessages(null);
			HashMap<String, Message> localUidMap = new HashMap<String, Message>();
			for (Message message : localMessages) {
				localUidMap.put(message.getUid(), message);
			}

			Store remoteStore = account.getRemoteStore();
			remoteFolder = remoteStore.getFolder(folderName);

			/*
			 * Open the remote folder. This pre-loads certain metadata like
			 * message count.
			 */
			remoteFolder.open(OpenMode.READ_WRITE);
			if (Account.EXPUNGE_ON_POLL.equals(account.getExpungePolicy())) {
				remoteFolder.expunge();
			}

			/*
			 * Get the remote message count.
			 */
			int remoteMessageCount = remoteFolder.getMessageCount();

			int visibleLimit = localFolder.getVisibleLimit();

			if (visibleLimit < 0) {
				visibleLimit = RakuPhotoMail.DEFAULT_VISIBLE_LIMIT;
			}

			Message[] remoteMessageArray = new Message[0];
			;
			final ArrayList<Message> remoteMessages = new ArrayList<Message>();
			HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();

			final Date earliestDate = account.getEarliestPollDate();

			if (remoteMessageCount > 0) {
				/* Message numbers start at 1. */
				int remoteStart;
				if (visibleLimit > 0) {
					remoteStart = Math
							.max(0, remoteMessageCount - visibleLimit) + 1;
				} else {
					remoteStart = 1;
				}
				int remoteEnd = remoteMessageCount;

				final AtomicInteger headerProgress = new AtomicInteger(0);

				remoteMessageArray = remoteFolder.getMessages(remoteStart,
						remoteEnd, earliestDate, null);

				for (Message thisMess : remoteMessageArray) {
					headerProgress.incrementAndGet();
					Message localMessage = localUidMap.get(thisMess.getUid());
					if (localMessage == null
							|| !localMessage.olderThan(earliestDate)) {
						remoteMessages.add(thisMess);
						remoteUidMap.put(thisMess.getUid(), thisMess);
					}
				}
				remoteMessageArray = null;

			} else if (remoteMessageCount < 0) {
				throw new Exception("Message count " + remoteMessageCount
						+ " for folder " + folderName);
			}

			/*
			 * Remove any messages that are in the local store but no longer on
			 * the remote store or are too old
			 */
			if (account.syncRemoteDeletions()) {
				ArrayList<Message> destroyMessages = new ArrayList<Message>();
				for (Message localMessage : localMessages) {
					if (remoteUidMap.get(localMessage.getUid()) == null) {
						destroyMessages.add(localMessage);
					}
				}

				localFolder.destroyMessages(destroyMessages
						.toArray(new Message[0]));

			}
			localMessages = null;

			setLocalFlaggedCountToRemote(localFolder, remoteFolder);

			/* Notify listeners that we're finally done. */
			localFolder.setLastChecked(System.currentTimeMillis());
			localFolder.setStatus(null);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeFolder(remoteFolder);
			closeFolder(tLocalFolder);
		}
	}

	private void setLocalFlaggedCountToRemote(LocalFolder localFolder,
			Folder remoteFolder) throws MessagingException {
		int remoteFlaggedMessageCount = remoteFolder.getFlaggedMessageCount();
		if (remoteFlaggedMessageCount != -1) {
			localFolder.setFlaggedMessageCount(remoteFlaggedMessageCount);
		} else {
			int flaggedCount = 0;
			Message[] messages = localFolder.getMessages(null, false);
			for (Message message : messages) {
				if (message.isSet(Flag.FLAGGED) && !message.isSet(Flag.DELETED)) {
					flaggedCount++;
				}
			}
			localFolder.setFlaggedMessageCount(flaggedCount);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gallery_mail_slide:
			try {
				onSlide();
			} catch (MessagingException e) {
				e.printStackTrace();
				Log.e("rakuphotomail", "Hello!Hello!Error! HAHAHAHA!", e);
			}
			break;
		case R.id.gallery_mail_reply:
			onReply();
			break;
		case R.id.gallery_mail_pre:
			onMailPre();
			break;
		case R.id.gallery_mail_next:
			onMailNext();
			break;
		case R.id.gallery_attachment_picuture_default:
			break;
		case R.id.gallery_attachment_picuture_even:
			// XXX りふぁくたりんぐしたい
			try {
				onSlideStop();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case R.id.gallery_attachment_picuture_odd:
			// XXX りふぁくたりんぐしたい
			try {
				onSlideStop();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			Log.w(RakuPhotoMail.LOG_TAG, "onClick is no Action !!!!");
		}
	}

	// XXX りふぁくたりんぐしたい
	private void onMailCurrent() {
		Log.d("neko", "onMailCurrent mMessageUid:" + mMessageUid);
		Log.d("neko",
				"onMailCurrent mMessageUids.size():" + mMessageUids.size());
		int currentIndex = mMessageUids.indexOf(mMessageUid);
		int maxIndex = mMessageUids.size() - 1;
		int minIndex = 0;
		if (currentIndex >= minIndex && currentIndex <= maxIndex) {
			Log.d("neko", "onMailCurrent maxIndex:" + maxIndex);
			setMailDisp(currentIndex);
			// if (currentIndex == maxIndex) {
			// mMailPre.setVisibility(View.GONE);
			// mMailSeparator1.setVisibility(View.GONE);
			// }
			// if (currentIndex == minIndex) {
			// mMailNext.setVisibility(View.GONE);
			// mMailSeparator3.setVisibility(View.GONE);
			// }
		} else {
			// XXX end
			Toast.makeText(GallerySlideShow.this, "メールが存在しません。",
					Toast.LENGTH_SHORT);
		}
	}

	// XXX りふぁくたりんぐしたい
	private void onMailPre() {
		Log.d("neko", "onMailPre mMessageUid:" + mMessageUid);
		int preIndex = mMessageUids.indexOf(mMessageUid) + 1;
		int maxIndex = mMessageUids.size() - 1;
		if (preIndex <= maxIndex) {
			setMailDisp(preIndex);
			if (preIndex == maxIndex) {
				mMailPre.setVisibility(View.GONE);
				mMailSeparator1.setVisibility(View.GONE);
			}
		} else {
			// XXX end
			Toast.makeText(GallerySlideShow.this, "メールが存在しません。",
					Toast.LENGTH_SHORT);
			mMailPre.setVisibility(View.GONE);
			mMailSeparator1.setVisibility(View.GONE);
		}
	}

	// XXX りふぁくたりんぐしたい
	private void onMailNext() {
		Log.d("neko", "onMailNext mMessageUid:" + mMessageUid);
		int nextIndex = mMessageUids.indexOf(mMessageUid) - 1;
		int minIndex = 0;
		if (nextIndex >= minIndex) {
			setMailDisp(nextIndex);
			if (nextIndex == minIndex) {
				mMailNext.setVisibility(View.GONE);
				mMailSeparator3.setVisibility(View.GONE);
			}
		} else {
			// XXX end
			Toast.makeText(GallerySlideShow.this, "メールが存在しません。",
					Toast.LENGTH_SHORT);
			mMailNext.setVisibility(View.GONE);
			mMailSeparator3.setVisibility(View.GONE);
		}
	}

	// XXX setNewMailInfoの一部と共通化できそう
	private void setMailDisp(int index) {
		Log.d("neko", "setMailDisp start index:" + index);
		setContentView(R.layout.gallery_slide_show_stop);
		setupViewsMailDetail();
		mMessageUid = mMessageUids.get(index);
		MessageBean message = mMessages.get(mMessageUid);
		CopyOnWriteArrayList<AttachmentBean> attachments = message
				.getAttachments();
		Bitmap bitmap = populateFromPart(attachments.get(0).getPart());
		if (bitmap == null) {
			return;
		}
		mImageViewPicture.setImageBitmap(bitmap);
		if (attachments.size() > 1) {
			ArrayList<Bitmap> list = new ArrayList<Bitmap>();
			for (AttachmentBean bean : attachments) {
				list.add(populateFromPartThumbnail(bean.getPart()));
			}
			mImageAdapter = new ImageAdapter(mContext);
			mImageAdapter.setImageItems(list);
			mGallery.setAdapter(mImageAdapter);
			mGallery.setOnItemClickListener((OnItemClickListener) mContext);
		} else {
			mGalleryLinearLayout.setVisibility(View.GONE);
		}
		int maxIndex = mMessageUids.size() - 1;
		int minIndex = 0;
		Log.d("neko", "setMailDisp mMessageUids.size():" + mMessageUids.size());
		Log.d("neko", "setMailDisp maxIndex:" + maxIndex);
		if (minIndex == index) {
			Log.d("neko", "setMailDisp minIndex");
			mMailNext.setVisibility(View.GONE);
			mMailSeparator3.setVisibility(View.GONE);
		} else if (maxIndex == index) {
			Log.d("neko", "setMailDisp maxIndex");
			mMailPre.setVisibility(View.GONE);
			mMailSeparator1.setVisibility(View.GONE);
		} else {
			// XXX none
		}
		setupViewMail(message);
	}

	private void onSlide() throws MessagingException {
		Log.d("neko", "onSlide start");
		if (null == mMessageUid || "".equals(mMessageUid)) {
			mMessageUid = messageBean.getUid();
		}
		setContentView(R.layout.gallery_slide_show);
		setupViews();
		List<MessageInfo> messageInfoList = getMessages();
		if (messageInfoList.size() > 0) {
			Log.d("neko",
					"onSlide messageInfoList.size():" + messageInfoList.size());
			setSlideInfo(messageInfoList);
			if (!messageSlideThread.isAlive()) {
				Log.d("neko", "onSlide messageSlideThread:"
						+ messageSlideThread + " isSlideRepeat:"
						+ isSlideRepeat);
				isSlideRepeat = true;
				restartMesasgeSlideThread = new Thread(messageSlide);
				restartMesasgeSlideThread.start();
			}
			if (null == checkMailThread) {
				isCheckRepeat = true;
				restartCheckMailThread = new Thread(checkMail);
				restartCheckMailThread.start();
			}
		} else {
			onStop();
		}
		Log.d("neko", "onSlide end");
	}

	private void onSlideStop() throws InterruptedException {
		Log.d("neko", "onSlideStop start");
		if (isSlideRepeat) {
			Log.d("neko", "onSlideStop isSlideRepeat:" + isSlideRepeat);
			repeatEnd();
			if (null != messageSlideThread && messageSlideThread.isAlive()) {
				Log.d("neko", "onSlideStop messageSlideThread:"
						+ messageSlideThread);
				messageSlideThread.join();
			}
			// XXX Checkhはスレッドすんのやめる予定
			if (null != checkMailThread && checkMailThread.isAlive()) {
				checkMailThread.join();
			}
			if (null != restartMesasgeSlideThread
					&& restartMesasgeSlideThread.isAlive()) {
				Log.d("neko", "onSlideStop restartMesasgeSlideThread:"
						+ restartMesasgeSlideThread);
				restartMesasgeSlideThread.join();
			}
			// XXX Checkhはスレッドすんのやめる予定
			if (null != restartCheckMailThread
					&& restartCheckMailThread.isAlive()) {
				restartCheckMailThread.join();
			}
			onMailCurrent();
		}
		Log.d("neko", "onSlideStop end");
	}

	private void onReply() {
		GallerySendingMail.actionReply(this, newMessageBean);
	}

	public class ImageAdapter extends BaseAdapter {
		private int mGalleryItemBackground;
		private Context mContext;
		private ArrayList<Bitmap> mImageItems = new ArrayList<Bitmap>();

		public ImageAdapter(Context c) {
			mContext = c;
			TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
			mGalleryItemBackground = a.getResourceId(
					R.styleable.Gallery1_android_galleryItemBackground, 0);
			a.recycle();

			mImageItems = setDroidList();
		}

		public int getCount() {
			return mImageItems.size();
		}

		public Object getItem(int position) {
			return mImageItems.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);
			Bitmap bitmap = (Bitmap) getItem(position);
			i.setImageBitmap(bitmap);
			i.setBackgroundResource(mGalleryItemBackground);

			return i;
		}

		public void setImageItems(ArrayList<Bitmap> imageItems) {
			this.mImageItems = imageItems;
		}

		public ArrayList<Bitmap> getImageItems() {
			return this.mImageItems;
		}

		// XXX まさにゴミ
		private ArrayList<Bitmap> setDroidList() {
			return new ArrayList<Bitmap>();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Toast.makeText(GallerySlideShow.this, "" + position, Toast.LENGTH_SHORT)
				.show();
		mImageViewPicture.setImageBitmap(populateFromPart(newAttachmentList
				.get(position).getPart()));
	}
}
