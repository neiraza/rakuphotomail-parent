package jp.co.fttx.rakuphotomail.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
import android.content.Context;
import android.content.Intent;
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
	private String mMessageUid = null;

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
	private boolean isSlideRepeat = true;
	private boolean isCheckRepeat = true;
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
	private long mDispAttachmentId;
	private long mDispMessageId;

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gallery_slide_show);
		setupViews();
		onNewIntent(getIntent());
		initThreading();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
		outState.putString(EXTRA_FOLDER, mFolderName);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mAccount = Preferences.getPreferences(this).getAccount(
				savedInstanceState.getString(EXTRA_ACCOUNT));
		mFolderName = savedInstanceState.getString(EXTRA_FOLDER);
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
		setIntent(intent);
		mAccount = Preferences.getPreferences(this).getAccount(
				intent.getStringExtra(EXTRA_ACCOUNT));
		mFolderName = intent.getStringExtra(EXTRA_FOLDER);
		// mController = MessagingController.getInstance(getApplication());
	}

	@Override
	public void onResume() {
		super.onResume();
		createMessageListThread.start();
		checkMailThread.start();
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
		handler = new Handler();
		setMailInfo = new Runnable() {
			@Override
			public void run() {
				setMailEffect(messageBean, attachmentBean);
			}
		};

		setMailInit = new Runnable() {
			@Override
			public void run() {
				setMailInit(mMessageInit, mAttachmentInit);
			}
		};

		// XXX setMailDisp(int index)の一部と共通化できそう
		setNewMailInfo = new Runnable() {
			@Override
			public void run() {
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
			}
		};

		createMessageList = new Runnable() {
			@Override
			public void run() {
				List<MessageInfo> messageInfoList = getMessages();
				if (messageInfoList.size() > 0) {
					setSlideInfo(messageInfoList);
					messageSlideThread.start();
				} else {
					onStop();
				}
			}
		};
		createMessageListThread = new Thread(createMessageList);

		messageSlide = new Runnable() {
			@Override
			public void run() {
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
			}
		};
		messageSlideThread = new Thread(messageSlide);

		checkMail = new Runnable() {
			@Override
			public void run() {
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
			}
		};
		checkMailThread = new Thread(checkMail);
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
		if (mMessageUids == null || mMessageUids.size() == 0) {
			onStop();
		}
		int index = checkMessageUid(mMessageUid);
		for (; index < mMessageUids.size(); index++) {
			mMessageUid = mMessageUids.get(index);
			messageBean = mMessages.get(mMessageUid);
			if (messageBean != null && isSlideRepeat) {
				CopyOnWriteArrayList<AttachmentBean> attachments = messageBean
						.getAttachments();
				// XXX 最後に表示したメッセージIDを引数に追加する
				mDispAttachmentId = slideShowAttachmentLoop(attachments,
						mDispMessageId, mDispAttachmentId);
				mDispMessageId = messageBean.getId();
			} else if (!isSlideRepeat) {
				return;
			} else {
				onStop();
			}
		}
		mMessageUid = mMessageUids.get(0);
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
		LocalStore localStore = null;
		LocalFolder localFolder = null;
		try {
			localStore = mAccount.getLocalStore();
			localFolder = localStore.getFolder(mFolderName);
			localFolder.open(OpenMode.READ_WRITE);
			return localStore.getMessages(localFolder.getId());
		} catch (MessagingException e) {
			e.printStackTrace();
			closeFolder(localFolder);
		}
		return null;
	}

	private void setSlideInfo(List<MessageInfo> messageInfoList) {
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
				CopyOnWriteArrayList<AttachmentBean> attachments = renderAttachments(message);
				if (attachments.size() > 0) {
					mb.setAttachments(attachments);
					mMessages.put(String.valueOf(uid), mb);
					mMessageUids.add(uid);
				}
			}
		}
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
	 * @author tooru.oguri
	 * @param account
	 * @param folder
	 * @param uid
	 * @return
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

	public CopyOnWriteArrayList<AttachmentBean> renderAttachments(Part part) {
		CopyOnWriteArrayList<AttachmentBean> attachments = null;
		if (part.getBody() instanceof Multipart) {
			try {
				attachments = splitMultipart(part);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		} else if (part instanceof LocalStore.LocalAttachmentBodyPart) {
			attachments = new CopyOnWriteArrayList<AttachmentBean>();
			AttachmentBean attachment = setAttachment(part);
			if (isSlide(attachment)) {
				attachments.add(attachment);
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
				attachments = splitMultipart(part);
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

	private CopyOnWriteArrayList<AttachmentBean> splitMultipart(Part part)
			throws MessagingException {
		Multipart mp = (Multipart) part.getBody();
		CopyOnWriteArrayList<AttachmentBean> attachments = new CopyOnWriteArrayList<AttachmentBean>();
		for (int i = 0; i < mp.getCount(); i++) {
			if (mp.getBodyPart(i) instanceof LocalStore.LocalAttachmentBodyPart) {
				AttachmentBean attachment = setAttachment(mp.getBodyPart(i));
				if (isSlide(attachment)) {
					attachments.add(setAttachment(mp.getBodyPart(i)));
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
		super.onDestroy();
		repeatEnd();
		createMessageListThread = null;
		messageSlideThread = null;
		checkMailThread = null;
		// finish();
	}

	private void repeatStart() {
		isSlideRepeat = true;
		isCheckRepeat = true;
	}

	private void repeatEnd() {
		isSlideRepeat = false;
		isCheckRepeat = false;
	}

	private boolean isSlide() {
		if (isSlideRepeat && isCheckRepeat) {
			return true;
		}
		return false;
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
		mMailSubject.setText(message.getSubject());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd h:mm a");
		// XXX sdf.format(message.getDate())????
		mMailDate.setText(sdf.format(message.getDate()));
		// XXX あれ機能してる？
		if (message.isFlagAnswered()) {
			mAnswered.setVisibility(View.VISIBLE);
		}
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
			onSlide();
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
			onSlideStop();
			break;
		case R.id.gallery_attachment_picuture_odd:
			// XXX りふぁくたりんぐしたい
			onSlideStop();
			break;
		default:
			Log.w(RakuPhotoMail.LOG_TAG, "onClick is no Action !!!!");
		}
	}

	// XXX りふぁくたりんぐしたい
	private void onMailCurrent() {
		int currentIndex = mMessageUids.indexOf(mMessageUid);
		int maxIndex = mMessageUids.size() - 1;
		int minIndex = 0;
		if (currentIndex >= minIndex && currentIndex <= maxIndex) {
			setMailDisp(currentIndex);
			if (currentIndex == maxIndex) {
				mMailPre.setVisibility(View.GONE);
				mMailSeparator1.setVisibility(View.GONE);
			}
			if (currentIndex == minIndex) {
				mMailNext.setVisibility(View.GONE);
				mMailSeparator3.setVisibility(View.GONE);
			}
		} else {
			// XXX end
			Toast.makeText(GallerySlideShow.this, "メールが存在しません。",
					Toast.LENGTH_SHORT);
		}

	}

	// XXX りふぁくたりんぐしたい
	private void onMailPre() {
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
		if (0 == index) {
			mMailNext.setVisibility(View.GONE);
			mMailSeparator3.setVisibility(View.GONE);
		} else if ((mMessageUids.size() - 1) == index) {
			mMailPre.setVisibility(View.GONE);
			mMailSeparator1.setVisibility(View.GONE);
		} else {
			// XXX none
		}
		setupViewMail(message);
	}

	private void onSlide() {
		if (null == mMessageUid || "".equals(mMessageUid)) {
			mMessageUid = messageBean.getUid();
		}
		setContentView(R.layout.gallery_slide_show);
		setupViews();
		repeatStart();
		List<MessageInfo> messageInfoList = getMessages();
		if (messageInfoList.size() > 0) {
			setSlideInfo(messageInfoList);
			restartMesasgeSlideThread = new Thread(messageSlide);
			restartMesasgeSlideThread.start();
			restartCheckMailThread = new Thread(checkMail);
			restartCheckMailThread.start();
		} else {
			onStop();
		}
	}

	private void onSlideStop() {
		if (isSlide()) {
			repeatEnd();
			// XXX こいつらはもう不要
			messageSlideThread = null;
			checkMailThread = null;
			// XXX スライドさんはいらんけど、チェックさんは停止中でも何かしたければ？
			restartMesasgeSlideThread = null;
			restartCheckMailThread = null;
			onMailCurrent();
		}
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
