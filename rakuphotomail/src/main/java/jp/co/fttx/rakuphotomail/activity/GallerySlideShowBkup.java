///*
// * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
// * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
// */
package jp.co.fttx.rakuphotomail.activity;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import jp.co.fttx.rakuphotomail.Account;
//import jp.co.fttx.rakuphotomail.Preferences;
//import jp.co.fttx.rakuphotomail.R;
//import jp.co.fttx.rakuphotomail.RakuPhotoMail;
//import jp.co.fttx.rakuphotomail.mail.FetchProfile;
//import jp.co.fttx.rakuphotomail.mail.Flag;
//import jp.co.fttx.rakuphotomail.mail.Folder;
//import jp.co.fttx.rakuphotomail.mail.Folder.OpenMode;
//import jp.co.fttx.rakuphotomail.mail.Message;
//import jp.co.fttx.rakuphotomail.mail.MessagingException;
//import jp.co.fttx.rakuphotomail.mail.Multipart;
//import jp.co.fttx.rakuphotomail.mail.Part;
//import jp.co.fttx.rakuphotomail.mail.Store;
//import jp.co.fttx.rakuphotomail.mail.internet.MimeUtility;
//import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
//import jp.co.fttx.rakuphotomail.mail.store.LocalStore.Attachments;
//import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalAttachmentBodyPart;
//import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalFolder;
//import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalMessage;
//import jp.co.fttx.rakuphotomail.mail.store.LocalStore.MessageInfo;
//import jp.co.fttx.rakuphotomail.mail.store.UnavailableStorageException;
//import jp.co.fttx.rakuphotomail.provider.AttachmentProvider;
//import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
//import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
//import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
//import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;
//import jp.co.fttx.rakuphotomail.rakuraku.util.Rotate3dAnimation;
//import jp.co.fttx.rakuphotomail.service.AttachmentSyncReceiver;
//import jp.co.fttx.rakuphotomail.service.AttachmentSyncService;
//import jp.co.fttx.rakuphotomail.service.GallerySlideReceiver;
//import jp.co.fttx.rakuphotomail.service.GallerySlideService;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.ServiceConnection;
//import android.content.res.TypedArray;
//import android.database.CursorIndexOutOfBoundsException;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.IBinder;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.Window;
//import android.view.animation.AccelerateInterpolator;
//import android.view.animation.Animation;
//import android.view.animation.Animation.AnimationListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.BaseAdapter;
//import android.widget.Gallery;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
///**
// * @author tooru.oguri
// * @since rakuphoto 0.1-beta1
// */
public class GallerySlideShowBkup {
//public class GallerySlideShowBkup extends RakuPhotoActivity implements View.OnClickListener,
//                OnItemClickListener {
//
//    private static final String EXTRA_ACCOUNT = "account";
//    private static final String EXTRA_FOLDER = "folder";
//
//    private Account mAccount;
//    private String mFolderName;
//    private MessageBean messageBean;
//    private AttachmentBean attachmentBean;
//    private MessageBean newMessageBean;
//    private CopyOnWriteArrayList<AttachmentBean> newAttachmentList;
//    private volatile String mMessageUid = null;
//
//    /*
//     * Slide
//     */
//    private TextView mSubject;
//    private ViewGroup mContainer;
//    private ImageView mImageViewDefault;
//    private ImageView mImageViewEven;
//    private ImageView mImageViewOdd;
//    /*
//     * new mail(detail mail)
//     */
//    private ImageView mImageViewPicture;
//    private TextView mMailSubject;
//    private TextView mMailDate;
//    private TextView mAnswered;
//    private TextView mMailPre;
//    private TextView mMailSeparator1;
//    private TextView mMailSlide;
//    private TextView mMailNext;
//    private TextView mMailSeparator3;
//    private TextView mMailReply;
//    private ImageAdapter mImageAdapter;
//    private LinearLayout mGalleryLinearLayout;
//    private Gallery mGallery;
//
//    /*
//     * anime
//     */
//    private float centerX;
//    private float centerY;
//    private int DURATION = 500;
//
//    private Thread createMessageListThread;
//    private Runnable createMessageList;
//    private volatile boolean isSlideRepeat = true;
//    private volatile boolean isCheckRepeat = true;
//    private Thread messageSlideThread;
//    private Runnable messageSlide;
//    private Handler handler;
//    private Runnable setMailInfo;
//    private Runnable setNewMailInfo;
//    // private Runnable checkMail;
//    private Runnable setMailInit;
//    // private Thread checkMailThread;
//    private Thread restartMesasgeSlideThread;
//    // private Thread restartCheckMailThread;
//    private Context mContext;
//
//    private CopyOnWriteArrayList<String> mMessageUids = new CopyOnWriteArrayList<String>();
//    private ConcurrentHashMap<String, MessageBean> mMessages = new ConcurrentHashMap<String, MessageBean>();
//    private MessageBean mMessageInit = new MessageBean();
//    private AttachmentBean mAttachmentInit = new AttachmentBean();
//    private volatile long mDispAttachmentId;
//    private volatile long mDispMessageId;
//
//    private AttachmentSyncService synqService;
//    private GallerySlideService slideService;
//    private boolean mIsBound = false;
//    private AttachmentSyncReceiver attachmentReceiver = new AttachmentSyncReceiver();
//    private GallerySlideReceiver slideReceiver = new GallerySlideReceiver();
//
//    private boolean newMailFlg = false;
//
//    public static void actionSlideShow(Context context, Account account, String folder) {
//        Log.d("maguro", "GallerySlideShow#actionHandlerFolder start");
//        Intent intent = actionHandleFolderIntent(context, account, folder);
//        context.startActivity(intent);
//        Log.d("maguro", "GallerySlideShow#actionHandlerFolder end");
//    }
//
//    public static Intent actionHandleFolderIntent(Context context, Account account, String folder) {
//        Log.d("maguro", "GallerySlideShow#actionHandlerFolderIntent start");
//        Intent intent = new Intent(context, GallerySlideShowBkup.class);
//        if (account != null) {
//            intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
//        }
//        if (folder != null) {
//            intent.putExtra(EXTRA_FOLDER, folder);
//        }
//        Log.d("maguro", "GallerySlideShow#actionHandlerFolderIntent end");
//        return intent;
//    }
//
//    private void doBindService() {
//        Log.d("maguro", "GallerySlideShow#doBindService start");
//        if (!mIsBound) {
//            mIsBound = bindService(getIntent(), mConnection, Context.BIND_AUTO_CREATE);
////            IntentFilter attachmenFilter = new IntentFilter(AttachmentSyncService.ACTION);
////            registerReceiver(attachmentReceiver, attachmenFilter);
//            IntentFilter slideFilter = new IntentFilter(GallerySlideService.ACTION);
//            registerReceiver(slideReceiver, slideFilter);
//        }
//        Log.d("maguro", "GallerySlideShow#doBindService end");
//    }
//
//    private void doUnbindService() {
//        Log.d("maguro", "GallerySlideShow#doUnBindService start");
//        if (mIsBound) {
//            unbindService(mConnection);
//            mIsBound = false;
////            unregisterReceiver(attachmentReceiver);
//            unregisterReceiver(slideReceiver);
//        }
//        Log.d("maguro", "GallerySlideShow#doUnBindService end");
//    }
//
//    private ServiceConnection mConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            Log.d("maguro", "GallerySlideShow#mConnection ServiceConnection#onServiceConnected");
////            synqService = ((AttachmentSyncService.AttachmentSyncBinder) service).getService();
//            slideService = ((GallerySlideService.GallerySlideBinder) service).getService();
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            Log.d("maguro", "GallerySlideShow#mConnection ServiceConnection#onServiceDisconnected");
////            synqService = null;
//            slideService = null;
//        }
//    };
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        Log.d("maguro", "GallerySlideShow#onCreate start");
//        super.onCreate(savedInstanceState);
//        mContext = this;
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.gallery_slide_show);
//        setupSlideShowViews();
//        onNewIntent(getIntent());
//        doBindService();
////        initThreading();
////        createMessageListThread.start();
//        // checkMailThread.start();
//        Log.d("maguro", "GallerySlideShow#onCreate end");
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        Log.d("maguro", "GallerySlideShow#onSaveInstanceState start");
//        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
//        outState.putString(EXTRA_FOLDER, mFolderName);
//        Log.d("maguro", "GallerySlideShow#onSaveInstanceState end");
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        Log.d("maguro", "GallerySlideShow#onRestoreInstanceState start");
//        super.onRestoreInstanceState(savedInstanceState);
//        mAccount = Preferences.getPreferences(this)
//            .getAccount(savedInstanceState.getString(EXTRA_ACCOUNT));
//        mFolderName = savedInstanceState.getString(EXTRA_FOLDER);
//        Log.d("maguro", "GallerySlideShow#onRestoreInstanceState end");
//    }
//
//    private void setupSlideShowViews() {
//        Log.d("maguro", "GallerySlideShow#setupSlideShowViews start");
//        mSubject = (TextView) findViewById(R.id.gallery_subject);
//        mContainer = (ViewGroup) findViewById(R.id.gallery_container);
//        mImageViewDefault = (ImageView) findViewById(R.id.gallery_attachment_picuture_default);
//        mImageViewDefault.setVisibility(View.VISIBLE);
//        mImageViewEven = (ImageView) findViewById(R.id.gallery_attachment_picuture_even);
//        mImageViewEven.setOnClickListener(this);
//        mImageViewEven.setVisibility(View.GONE);
//        mImageViewOdd = (ImageView) findViewById(R.id.gallery_attachment_picuture_odd);
//        mImageViewOdd.setOnClickListener(this);
//        mImageViewOdd.setVisibility(View.GONE);
//        Log.d("maguro", "GallerySlideShow#setupSlideShowViews end");
//    }
//
//    private void setupSlideStopViews() {
//        Log.d("maguro", "GallerySlideShow#setupSlideStopViews start");
//        mGalleryLinearLayout = (LinearLayout) findViewById(R.id.gallery_mail_picture_slide_linear_layoput);
//        mImageViewPicture = (ImageView) findViewById(R.id.gallery_mail_picuture);
//        Log.d("download_test", "mImageViewPicture:" + mImageViewPicture);
//        Log.d("download_test", "View.VISIBLE:" + View.VISIBLE);
//        mImageViewPicture.setVisibility(View.VISIBLE);
//        mMailSubject = (TextView) findViewById(R.id.gallery_mail_subject);
//        mMailDate = (TextView) findViewById(R.id.gallery_mail_date);
//        mAnswered = (TextView) findViewById(R.id.gallery_mail_sent_flag);
//        mAnswered.setVisibility(View.GONE);
//        mMailPre = (TextView) findViewById(R.id.gallery_mail_pre);
//        mMailPre.setOnClickListener(this);
//        mMailSeparator1 = (TextView) findViewById(R.id.gallery_mail_separator1);
//        mMailSlide = (TextView) findViewById(R.id.gallery_mail_slide);
//        mMailSlide.setOnClickListener(this);
//        mMailReply = (TextView) findViewById(R.id.gallery_mail_reply);
//        mMailReply.setOnClickListener(this);
//        mMailSeparator3 = (TextView) findViewById(R.id.gallery_mail_separator3);
//        mMailNext = (TextView) findViewById(R.id.gallery_mail_next);
//        mMailNext.setOnClickListener(this);
//        mGallery = (Gallery) findViewById(R.id.gallery_mail_picture_slide);
//        Log.d("maguro", "GallerySlideShow#setupSlideStopViews end");
//    }
//
//    @Override
//    public void onNewIntent(Intent intent) {
//        Log.d("maguro", "GallerySlideShow#onNewIntent start");
//        intent.setClass(mContext, AttachmentSyncService.class);
//        intent.setClass(mContext, GallerySlideService.class);
//        setIntent(intent);
//        mAccount = Preferences.getPreferences(this).getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
//        mFolderName = intent.getStringExtra(EXTRA_FOLDER);
//        Log.d("maguro", "GallerySlideShow#onNewIntent end");
//    }
//
//    @Override
//    public void onResume() {
//        Log.d("maguro", "GallerySlideShow#onResume start");
//        super.onResume();
//        Log.d("maguro", "GallerySlideShow#onResume end");
//    }
//
//    private void setMailInit(MessageBean message, AttachmentBean attachment) {
//        Log.d("maguro", "GallerySlideShow#setMailInit start");
//        Bitmap bitmap = populateFromPart(attachment.getPart());
//        if (bitmap == null) {
//            Log.d("maguro", "GallerySlideShow#setMailInit bitmap is null");
//            return;
//        }
//        mImageViewDefault.setVisibility(View.GONE);
//        mImageViewEven.setVisibility(View.VISIBLE);
//        mImageViewEven.setImageBitmap(bitmap);
//        mSubject.setText(message.getSubject());
//        Log.d("maguro", "GallerySlideShow#setMailInit end");
//    }
//
//    private void setMailEffect(MessageBean message, AttachmentBean attachment) {
//        Log.d("maguro", "GallerySlideShow#setMailEffect start");
//        Bitmap bitmap = populateFromPart(attachment.getPart());
//        if (bitmap == null) {
//            Log.d("maguro", "GallerySlideShow#setMailEffect bitmap is null");
//            return;
//        }
//        applyRotation(bitmap);
//        mSubject.setText(message.getSubject());
//        Log.d("maguro", "GallerySlideShow#setMailEffect end");
//    }
//
//    private void reloadMessage() {
//        Log.d("maguro", "GallerySlideShow#reloadMessage start");
//        LocalStore localStore = null;
//        LocalFolder localFolder = null;
//        ArrayList<MessageInfo> list = new ArrayList<MessageInfo>();
//        try {
//            localStore = mAccount.getLocalStore();
//            localFolder = localStore.getFolder(mFolderName);
//            localFolder.open(OpenMode.READ_WRITE);
//            Log.d("maguro", "GallerySlideShow#reloadMessage mMessageUids.size():" + mMessageUids.size());
//            list = localStore.getMessages(mMessageUids, localFolder.getId());
//            setSlideInfo(list);
//            Log.d("maguro", "GallerySlideShow#reloadMessage normal end");
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
//            // FIXME これで大丈夫か？
//            // mMessages.clear();
//            // mMessageUids.clear();
//            Log.d("maguro", "GallerySlideShow#reloadMessage MessagingException end");
//        } catch (StringIndexOutOfBoundsException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
//            // FIXME これで大丈夫か？
//            // mMessages.clear();
//            // mMessageUids.clear();
//            Log.d("maguro", "GallerySlideShow#reloadMessage StringIndexOutOfBoundsException end");
//        }
//    }
//
//    private void initThreading() {
//        Log.d("maguro", "GallerySlideShow#initThreading start");
//        handler = new Handler();
//        setMailInfo = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("maguro", "GallerySlideShow#initThreading setMailInfo");
//                setMailEffect(messageBean, attachmentBean);
//            }
//        };
//
//        setMailInit = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("maguro", "GallerySlideShow#initThreading setMailInit");
//                setMailInit(mMessageInit, mAttachmentInit);
//            }
//        };
//
//        // XXX setMailDisp(int index)の一部と共通化できそう
//        setNewMailInfo = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("maguro", "GallerySlideShow#initThreading setNewMailInfo start");
//                setContentView(R.layout.gallery_slide_show_stop);
//                setupSlideStopViews();
//                Bitmap bitmap = populateFromPart(newAttachmentList.get(0).getPart());
//                if (bitmap == null) {
//                    Log.d("maguro", "GallerySlideShow#initThreading setNewMailInfo bitmap is null");
//                    return;
//                }
//                mImageViewPicture.setImageBitmap(bitmap);
//                if (newAttachmentList.size() > 1) {
//                    ArrayList<Bitmap> list = new ArrayList<Bitmap>();
//                    for (AttachmentBean bean : newAttachmentList) {
//                        list.add(populateFromPartThumbnail(bean.getPart()));
//                    }
//                    mImageAdapter = new ImageAdapter(mContext);
//                    mImageAdapter.setImageItems(list);
//                    mGallery.setAdapter(mImageAdapter);
//                    mGallery.setOnItemClickListener((OnItemClickListener) mContext);
//                } else {
//                    mGalleryLinearLayout.setVisibility(View.GONE);
//                }
//                mMessageUid = newMessageBean.getUid();
//                mMessageUids.add(0, mMessageUid);
//                mMessages.put(mMessageUid, newMessageBean);
//                if (0 >= mMessageUids.indexOf(mMessageUid)) {
//                    mMailNext.setVisibility(View.GONE);
//                    mMailSeparator3.setVisibility(View.GONE);
//                }
//                setupViewMail(newMessageBean);
//                newMailFlg = true;
//                Log.d("maguro", "GallerySlideShow#initThreading setNewMailInfo end");
//            }
//        };
//
//        createMessageList = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("maguro", "GallerySlideShow#initThreading createMessageList start");
//                List<MessageInfo> messageInfoList = getMessages();
//                Log.d("maguro", "initThreading createMessageList messageInfoList.size():"
//                    + messageInfoList.size());
//                if (messageInfoList.size() > 0) {
//                    setSlideInfo(messageInfoList);
//                    messageSlideThread.start();
//                } else {
//                    Log.w("maguro", "initThreading createMessageList onStop");
//                    onStop();
//                }
//                Log.d("maguro", "GallerySlideShow#initThreading createMessageList end");
//            }
//        };
//        createMessageListThread = new Thread(createMessageList);
//
//        messageSlide = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("maguro", "GallerySlideShow#initThreading messageSlide start");
//                checkMessageUid(mMessageUid);
//                mMessageInit = mMessages.get(mMessageUid);
//                mAttachmentInit = mMessageInit.getAttachments().get(0);
//                handler.post(setMailInit);
//                mDispMessageId = mMessageInit.getId();
//                mDispAttachmentId = mAttachmentInit.getId();
//
//                while (isSlideRepeat) {
//                    // FIXME ここで最新化とかいらないし、バグの温床だわ←全がえじゃなければよくね？
//                    // 1 メールを再取得しようぜ
//                    reloadMessage();
//                    // 2 ありたっけのメールが対象
//                    slideShowStart();
//                }
//                Log.d("maguro", "GallerySlideShow#initThreading messageSlide end");
//            }
//        };
//        messageSlideThread = new Thread(messageSlide);
//
//        // checkMail = new Runnable() {
//        // @Override
//        // public void run() {
//        // Log.d("maguro", "GallerySlideShow#initThreading checkMail start");
//        // ArrayList<MessageBean> messages = new ArrayList<MessageBean>();
//        // while (isCheckRepeat) {
//        // sleep(30000);
//        // synchronizeMailbox(mAccount, mFolderName);
//        // messages = getNewMail();
//        // if (messages.size() > 0) {
//        // repeatEnd();
//        // }
//        // }
//        // if (messages.size() > 0) {
//        // slideShowNewMailStart(messages);
//        // }
//        // Log.d("maguro", "GallerySlideShow#initThreading checkMail end");
//        // }
//        // };
//        // checkMailThread = new Thread(checkMail);
//        Log.d("maguro", "GallerySlideShow#initThreading end");
//    }
//
//    private ArrayList<MessageBean> getNewMail() {
//        Log.d("maguro", "GallerySlideShow#getNewMail start");
    //        List<MessageInfo> newMessages = getMessages();
//        ArrayList<MessageBean> messages = new ArrayList<MessageBean>();
//        if (newMessages.size() > 0) {
//            try {
//                setNewMail(newMessages, messages);
//            } catch (RakuRakuException e) {
//                e.printStackTrace();
//                Log.e(RakuPhotoMail.LOG_TAG, "error:" + e);
//                messages.clear();
//            } catch (MessagingException e) {
//                e.printStackTrace();
//                Log.e(RakuPhotoMail.LOG_TAG, "error:" + e);
//                messages.clear();
//            }
//        }
//        Log.d("maguro", "GallerySlideShow#getNewMail end");
//        return messages;
//    }
//
//    private void setNewMail(List<MessageInfo> src, ArrayList<MessageBean> dest) throws RakuRakuException,
//                    MessagingException {
//        Log.d("maguro", "GallerySlideShow#setNewMail start");
//        for (MessageInfo newMessage : src) {
//            String uid = newMessage.getUid();
//            if (!mMessageUids.contains(uid)) {
//                LocalMessage message = loadMessage(mAccount, mFolderName, uid);
//                MessageBean mb = setMessage(message, newMessage);
//                setNewMailAttachment(mb, message, dest);
//            }
//        }
//        Log.d("maguro", "GallerySlideShow#setNewMail end");
//    }
//
//    private void setNewMailAttachment(MessageBean mb, LocalMessage message, ArrayList<MessageBean> dest)
//                    throws RakuRakuException, MessagingException {
//        Log.d("maguro", "GallerySlideShow#setNewMailAttachment start");
//        if (mb.getAttachmentCount() > 0) {
//            Log.d("maguro", "GallerySlideShow#setNewMailAttachment start");
//            CopyOnWriteArrayList<AttachmentBean> attachments = renderAttachmentsNewMail(message);
//            if (attachments != null && attachments.size() > 0) {
//                mb.setAttachments(attachments);
//                dest.add(mb);
//            }
//        }
//        Log.d("maguro", "GallerySlideShow#setNewMailAttachment end");
//    }
//
//    private void slideShowNewMailStart(ArrayList<MessageBean> messages) {
//        Log.d("maguro", "GallerySlideShow#slideShowNewMailStart start");
//        newMessageBean = messages.get(0);
//        newAttachmentList = newMessageBean.getAttachments();
//        handler.post(setNewMailInfo);
//        Log.d("maguro", "GallerySlideShow#slideShowNewMailStart end");
//    }
//
//    private int checkMessageUid(String messageUid) {
//        Log.d("maguro", "GallerySlideShow#checkMessageUid start");
//        int index = mMessageUids.indexOf(messageUid);
//        Log.d("maguro", "GallerySlideShow#checkMessageUid index:" + index);
//        try {
//            if (index == -1) {
//                index = 0;
//                Log.d("maguro",
//                    "GallerySlideShow#checkMessageUid mMessageUids.size():" + mMessageUids.size());
//                this.mMessageUid = mMessageUids.get(index);
//            }
//            Log.d("maguro", "GallerySlideShow#checkMessageUid nomarl end");
//            return index;
//        } catch (ArrayIndexOutOfBoundsException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
//            return -1;
//        }
//    }
//
//    private void slideShowStart() {
//        Log.d("maguro", "GallerySlideShow#slideShowStart start");
//        if (mMessageUids == null || mMessageUids.size() == 0) {
//            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#slideShowStart onStop() mMessageUidsが存在しない");
//            onStop();
//            return;
//        }
//        int index = checkMessageUid(mMessageUid);
//        if (0 > index) {
//            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#slideShowStart mMessageUid(" + mMessageUid
//                + ")該当するデータが存在しない");
//            return;
//        }
//        for (; index < mMessageUids.size(); index++) {
//            if (isSlideRepeat) {
//                mMessageUid = mMessageUids.get(index);
//                messageBean = mMessages.get(mMessageUid);
//                if (messageBean != null) {
//                    CopyOnWriteArrayList<AttachmentBean> attachments = messageBean.getAttachments();
//                    Log.d("maguro", "GallerySlideShow#slideShowStart mMessageUid:" + mMessageUid
//                        + " messageBean.getId():" + messageBean.getId());
//                    mDispAttachmentId = slideShowAttachmentLoop(attachments, mDispMessageId,
//                        mDispAttachmentId);
//                    mDispMessageId = messageBean.getId();
//                } else {
//                    onStop();
//                }
//            } else {
//                Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#slideShowStart isSlideRepeat("
//                    + isSlideRepeat + ")スライドショーの停止要求が発生しています");
//                return;
//            }
//        }
//        if (isSlideRepeat) {
//            mMessageUid = mMessageUids.get(0);
//        }
//        Log.d("maguro", "GallerySlideShow#slideShowStart end");
//    }
//
//    private long slideShowAttachmentLoop(CopyOnWriteArrayList<AttachmentBean> attachments,
//                    long dispMessageId, long dispAttachmentId) {
//        Log.d("maguro", "GallerySlideShow#slideShowAttachmentLoop start");
//        long result = 0;
//        for (AttachmentBean attachment : attachments) {
//            Log.d("maguro", "GallerySlideShow#slideShowAttachmentLoop dispMessageId:" + dispMessageId
//                + " attachment.getId():" + attachment.getId());
//            // 前スライドと同一データ時は切り替えない
//            if (messageBean.getId() != dispMessageId && attachment.getId() != dispAttachmentId) {
//                attachmentBean = attachment;
//                handler.post(setMailInfo);
//                result = attachment.getId();
//            }
//            sleep(10000);
//        }
//        Log.d("maguro", "GallerySlideShow#slideShowAttachmentLoop end");
//        return result;
//    }
//
//    private List<MessageInfo> getMessages() {
//        Log.d("maguro", "GallerySlideShow#getMessages start");
//        LocalStore localStore = null;
//        LocalFolder localFolder = null;
//        try {
//            localStore = mAccount.getLocalStore();
//            localFolder = localStore.getFolder(mFolderName);
//            localFolder.open(OpenMode.READ_WRITE);
//            Log.d("maguro", "GallerySlideShow#getMessages nomal end");
//            return localStore.getMessages(localFolder.getId());
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            closeFolder(localFolder);
//        }
//        Log.d("maguro", "GallerySlideShow#getMessages abnomal end");
//        return null;
//    }
//
//    private void setSlideInfo(List<MessageInfo> messageInfoList) {
//        Log.d("maguro", "GallerySlideShow#setSlideInfo start");
//        mMessages.clear();
//        mMessageUids.clear();
//        Log.d("maguro", "GallerySlideShow#setSlideInfo messageInfoList.size():" + messageInfoList.size());
//        for (MessageInfo messageInfo : messageInfoList) {
//            String uid = messageInfo.getUid();
//            Log.d("maguro", "GallerySlideShow#setSlideInfo uid:" + uid);
//            LocalMessage message = loadMessage(mAccount, mFolderName, uid);
//            if (message == null) {
//                Log.d("maguro", "GallerySlideShow#setSlideInfo message is null");
//                onDestroy();
//            }
//            Log.d(
//                "maguro",
//                "GallerySlideShow#setSlideInfo message.getAttachmentCount():"
//                    + message.getAttachmentCount());
//            if (message.getAttachmentCount() > 0) {
//                MessageBean mb = setMessage(message, messageInfo);
//                Log.d("maguro", "GallerySlideShow#setSlideInfo mb.getUid():" + mb.getUid()
//                    + " mb.getId():" + mb.getId());
//                CopyOnWriteArrayList<AttachmentBean> attachments;
//                attachments = renderAttachments(message, message.getUid());
//                if (attachments.size() > 0) {
//                    mb.setAttachments(attachments);
//                    mMessages.put(String.valueOf(uid), mb);
//                    mMessageUids.add(uid);
//                }
//            }
//        }
//        Log.d("maguro", "GallerySlideShow#setSlideInfo end");
//    }
//
//    private MessageBean setMessage(LocalMessage message, MessageInfo messageInfo) {
//        Log.d("maguro", "GallerySlideShow#setMessage start");
//        MessageBean messageBean = new MessageBean();
//        messageBean.setId(message.getId());
//        messageBean.setSubject(message.getSubject());
//        messageBean.setUid(message.getUid());
//        messageBean.setAttachmentCount(message.getAttachmentCount());
//        messageBean.setDate(messageInfo.getDate());
//        messageBean.setTextContent(messageInfo.getTextContent());
//        messageBean.setSenderList(messageInfo.getSenderList());
//        String[] mailFromArr = messageInfo.getSenderList().split(";");
//        if (mailFromArr == null || mailFromArr.length == 0) {
//        } else if (mailFromArr.length == 1) {
//            messageBean.setSenderAddress(mailFromArr[0]);
//        } else {
//            messageBean.setSenderAddress(mailFromArr[0]);
//            messageBean.setSenderName(mailFromArr[1]);
//        }
//        messageBean.setToList(messageInfo.getToList());
//        messageBean.setCcList(messageInfo.getCcList());
//        messageBean.setBccList(messageInfo.getBccList());
//        messageBean.setMessageId(messageInfo.getMessageId());
//        messageBean.setMessage(message);
//        // [X_GOT_ALL_HEADERS,X_DOWNLOADED_FULL,SEEN,ANSWERED,X_DOWNLOADED_PARTIAL,X_REMOTE_COPY_STARTED]
//        String flags = messageInfo.getFlags();
//        messageBean.setFlags(flags);
//        String[] flagList = RakuPhotoStringUtils.splitFlags(flags);
//        if (null != flagList && flagList.length != 0) {
//            setFlag(flagList, messageBean);
//        }
//        Log.d("maguro", "GallerySlideShow#setMessage end");
//        return messageBean;
//    }
//
//    /**
//     * setFlag.
//     * <ul>
//     * <li>X_GOT_ALL_HEADERS</li>
//     * <li>X_DOWNLOADED_FULL</li>
//     * <li>SEEN</li>
//     * <li>ANSWERED</li>
//     * <li>X_DOWNLOADED_PARTIAL</li>
//     * <li>X_REMOTE_COPY_STARTED</li>
//     * </ul>
//     *
//     * @param flag
//     */
//    private void setFlag(String[] flag, MessageBean messageBean) {
//        Log.d("maguro", "GallerySlideShow#setFlag start");
//        StringBuilder builder = new StringBuilder();
//        for (String f : flag) {
//            if ("X_GOT_ALL_HEADERS".equals(f)) {
//                messageBean.setFlagXGotAllHeaders(true);
//            } else if ("SEEN".equals(f)) {
//                messageBean.setFlagSeen(true);
//            } else if ("ANSWERED".equals(f)) {
//                messageBean.setFlagAnswered(true);
//            } else if ("X_DOWNLOADED_FULL".equals(f)) {
//                messageBean.setFlagXDownLoadedFull(true);
//            } else if ("X_DOWNLOADED_PARTIAL".equals(f)) {
//                messageBean.setFlagXDownLoadedPartial(true);
//            } else if ("X_REMOTE_COPY_STARTED".equals(f)) {
//                messageBean.setFlagXRemoteCopyStarted(true);
//            } else {
//                builder.append(f + ",");
//            }
//            int len = builder.length();
//            if (0 != len) {
//                messageBean.setFlagOther(builder.delete(len - 1, len).toString());
//            }
//        }
//        Log.d("maguro", "GallerySlideShow#setFlag end");
//    }
//
//    /**
//     * loadMessage. Mail to get the BodyPart are not stored in the SQLite.
//     *
//     * @param account
//     * @param folder
//     * @param uid
//     * @return
//     * @author tooru.oguri
//     */
//    private LocalMessage loadMessage(final Account account, final String folder, final String uid) {
//        try {
//            Log.d("maguro", "GallerySlideShow#loadMessage start");
//            LocalStore localStore = account.getLocalStore();
//            LocalFolder localFolder = localStore.getFolder(folder);
//            localFolder.open(OpenMode.READ_WRITE);
//            LocalMessage message = (LocalMessage) localFolder.getMessage(uid);
//
//            FetchProfile fp = new FetchProfile();
//            fp.add(FetchProfile.Item.ENVELOPE);
//            fp.add(FetchProfile.Item.BODY);
//            localFolder.fetch(new Message[] { message }, fp, null);
//            localFolder.close();
//            Log.d("maguro", "GallerySlideShow#loadMessage normal end");
//            return message;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Log.d("maguro", "GallerySlideShow#loadMessage abnormal end");
//        return null;
//    }
//
//    public CopyOnWriteArrayList<AttachmentBean> renderAttachments(Part part, String uid) {
//        Log.d("maguro", "GallerySlideShow#renderAttachments start");
//        CopyOnWriteArrayList<AttachmentBean> attachments = new CopyOnWriteArrayList<AttachmentBean>();
//        try {
//            if (part.getBody() instanceof Multipart) {
//                attachments = splitMultipart(part, uid);
//            } else if (part instanceof LocalAttachmentBodyPart) {
//                attachments = new CopyOnWriteArrayList<AttachmentBean>();
//                AttachmentBean attachment = setAttachment(part);
//                if (isSlide(attachment)) {
//                    attachments.add(attachment);
//                    Log.d("maguro", "GallerySlideShow#renderAttachments uid:" + uid);
//                    synqService.onDownload(mAccount, mFolderName, uid);
//                }
//            } else {
//                Log.d("maguro", "GallerySlideShow#renderAttachments return 1");
//                return attachments;
//            }
//            Log.d("maguro", "GallerySlideShow#renderAttachments return 2");
//            return attachments;
//        } catch (RakuRakuException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "error:" + e);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "error:" + e);
//        }
//        Log.d("maguro", "GallerySlideShow#renderAttachments return 3");
//        return attachments;
//    }
//
//    public CopyOnWriteArrayList<AttachmentBean> renderAttachmentsNewMail(Part part)
//                    throws RakuRakuException, MessagingException {
//        Log.d("maguro", "GallerySlideShow#renderAttachmentsNewMail start");
//        CopyOnWriteArrayList<AttachmentBean> attachments = new CopyOnWriteArrayList<AttachmentBean>();
//        if (part.getBody() instanceof Multipart) {
//            Log.d("maguro", "GallerySlideShow#renderAttachmentsNewMail Multipart");
//            try {
//                attachments = splitMultipart(part, null);
//            } catch (MessagingException e) {
//                e.printStackTrace();
//            }
//        } else if (part instanceof LocalAttachmentBodyPart) {
//            Log.d("maguro", "GallerySlideShow#renderAttachmentsNewMail not Multipart");
//            AttachmentBean attachment = setAttachment(part);
//            if (isSlide(attachment)) {
//                attachments.add(attachment);
//            }
//        }
//        Log.d("maguro", "GallerySlideShow#renderAttachmentsNewMail end");
//        return attachments;
//    }
//
//    private CopyOnWriteArrayList<AttachmentBean> splitMultipart(Part part, String uid)
//                    throws MessagingException, RakuRakuException {
//        Log.d("maguro", "GallerySlideShow#splitMultipart start");
//        Multipart mp = (Multipart) part.getBody();
//        CopyOnWriteArrayList<AttachmentBean> attachments = new CopyOnWriteArrayList<AttachmentBean>();
//        for (int i = 0; i < mp.getCount(); i++) {
//            if (mp.getBodyPart(i) instanceof LocalAttachmentBodyPart) {
//                AttachmentBean attachment = setAttachment(mp.getBodyPart(i));
//                if (isSlide(attachment)) {
//                    attachments.add(setAttachment(mp.getBodyPart(i)));
//                    if (null != uid) {
//                        Log.d("maguro", "GallerySlideShow#splitMultipart uid:" + uid);
//                        synqService.onDownload(mAccount, mFolderName, uid);
//                    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();    //To change body of overridden methods use File | Settings | File Templates.
    }
//                }
//            }
//        }
//        Log.d("maguro", "GallerySlideShow#splitMultipart end");
//        return attachments;
//    }
//
//    private AttachmentBean setAttachment(Part part) throws RakuRakuException, MessagingException {
//        Log.d("maguro", "GallerySlideShow#setAttachment start");
//        AttachmentBean attachment = new AttachmentBean();
//        String contentDisposition = null;
//        contentDisposition = MimeUtility.unfoldAndDecode(part.getDisposition());
//        if (contentDisposition != null
//            && MimeUtility.getHeaderParameter(contentDisposition, null).matches("^(?i:inline)")
//            && part.getHeader("Content-ID") != null) {
//            Log.d("maguro", "GallerySlideShow#setAttachment null");
//            return attachment;
//        }
//        LocalAttachmentBodyPart labPart = (LocalAttachmentBodyPart) part;
//        long attachmentId = labPart.getAttachmentId();
//        attachment.setId(attachmentId);
//        Attachments attachments = getAttachment(attachmentId);
//        if (attachments == null) {
//
//        }
//        attachment.setMimeType(attachments.getMimeType());
//        attachment.setName(attachments.getName());
//        attachment.setSize(Integer.valueOf(attachments.getSize()));
//        attachment.setPart(labPart);
//        Log.d("maguro", "GallerySlideShow#setAttachment null");
//        return attachment;
//    }
//
//    private Attachments getAttachment(long attachmentId) throws RakuRakuException {
//        Log.d("maguro", "GallerySlideShow#getAttachment start");
//        LocalStore localStore = null;
//        try {
//            localStore = mAccount.getLocalStore();
//            Log.d("maguro", "GallerySlideShow#getAttachment normal end");
//            return localStore.getAttachment(attachmentId);
//        } catch (CursorIndexOutOfBoundsException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "CursorIndexOutOfBoundsException:" + e);
//            throw new RakuRakuException("attachmentId:" + attachmentId, e);
//        } catch (UnavailableStorageException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "UnavailableStorageException:" + e);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "MessagingException:" + e);
//        }
//        Log.d("maguro", "GallerySlideShow#getAttachment abnormal end");
//        return null;
//    }
//
//    private void closeFolder(Folder f) {
//        Log.d("maguro", "GallerySlideShow#closeFolder start");
//        if (f != null) {
//            f.close();
//        }
//        Log.d("maguro", "GallerySlideShow#closeFolder end");
//    }
//
//    private Bitmap populateFromPart(LocalAttachmentBodyPart part) {
//        Log.d("maguro", "GallerySlideShow#populateFromPart start");
//        Bitmap bitmapView = null;
//        try {
//            bitmapView = getBitmapView(part);
//        } catch (Exception e) {
//            Log.e(RakuPhotoMail.LOG_TAG, "error ", e);
//            e.printStackTrace();
//        }
//        Log.d("maguro", "GallerySlideShow#populateFromPart end");
//        return bitmapView;
//    }
//
//    private Bitmap populateFromPartThumbnail(LocalAttachmentBodyPart part) {
//        Log.d("maguro", "GallerySlideShow#populateFromPartThumbnail start");
//        Bitmap bitmapView = null;
//        try {
//            bitmapView = getThumbnailBitmapView(part);
//        } catch (Exception e) {
//            Log.e(RakuPhotoMail.LOG_TAG, "error ", e);
//            e.printStackTrace();
//        }
//        Log.d("maguro", "GallerySlideShow#populateFromPartThumbnail end");
//        return bitmapView;
//    }
//
//    private Bitmap getBitmapView(LocalAttachmentBodyPart part) {
//        Log.d("maguro", "GallerySlideShow#getBitmapView start");
//        try {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            Uri uri = AttachmentProvider.getAttachmentUri(mAccount, part.getAttachmentId());
//            options.inJustDecodeBounds = true;
//            this.getContentResolver().openInputStream(uri);
//            BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, options);
//            int displayW = getWindowManager().getDefaultDisplay().getWidth();
//            int displayH = getWindowManager().getDefaultDisplay().getHeight();
//            int scaleW = options.outWidth / displayW + 1;
//            int scaleH = options.outHeight / displayH + 1;
//            options.inJustDecodeBounds = false;
//            options.inSampleSize = Math.max(scaleW, scaleH);
//            Log.d("maguro", "GallerySlideShow#getBitmapView normal end");
//            return BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null,
//                options);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("rakuphotomail", "Exception:" + e);
//        }
//        Log.d("maguro", "GallerySlideShow#getBitmapView abnormal end");
//        return null;
//    }
//
//    private Bitmap getThumbnailBitmapView(LocalAttachmentBodyPart part) {
//        Log.d("maguro", "GallerySlideShow#getThumbnailBitmapView start");
//        try {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            Uri uri = AttachmentProvider.getAttachmentUri(mAccount, part.getAttachmentId());
//            options.inJustDecodeBounds = true;
//            this.getContentResolver().openInputStream(uri);
//            BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, options);
//            // XXX 最終的にこのレイアウトサイズでOKOK？
//            int displayW = 150;
//            int displayH = 100;
//            int scaleW = options.outWidth / displayW + 1;
//            int scaleH = options.outHeight / displayH + 1;
//            options.inJustDecodeBounds = false;
//            options.inSampleSize = Math.max(scaleW, scaleH);
//            Log.d("maguro", "GallerySlideShow#getThumbnailBitmapView normal end");
//            return BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null,
//                options);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e(RakuPhotoMail.LOG_TAG, "Error:" + e);
//        }
//        Log.d("maguro", "GallerySlideShow#getThumbnailBitmapView abnormal end");
//        return null;
//    }
//
//    private boolean isSlide(AttachmentBean attachment) {
//        Log.d("maguro", "GallerySlideShow#isSlide start");
//        String mimeType = attachment.getMimeType();
//        String fileName = attachment.getName();
//        Log.d("maguro", "GallerySlideShow#isSlide end");
//        return "image/jpeg".equals(mimeType) || "image/png".equals(mimeType)
//            || (null != fileName && (fileName.endsWith(".png") || fileName.endsWith(".JPG")));
//    }
//
//    private void sleep(long time) {
//        Log.d("maguro", "GallerySlideShow#sleep start");
//        try {
//            Thread.sleep(time);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Log.d("maguro", "GallerySlideShow#sleep end");
//    }
//
//    @Override
//    public void onStop() {
//        Log.d("maguro", "GallerySlideShow#onStop start");
//        super.onStop();
//        repeatEnd();
//        Log.d("maguro", "GallerySlideShow#onStop end");
//    }
//
//    @Override
//    public void onDestroy() {
//        Log.d("maguro", "GallerySlideShow#onDestroy start");
//        super.onDestroy();
//        repeatEnd();
//        createMessageListThread = null;
//        messageSlideThread = null;
//        // checkMailThread = null;
//        doUnbindService();
//        Log.d("maguro", "GallerySlideShow#onDestroy end");
//    }
//
//    private void repeatEnd() {
//        Log.d("maguro", "GallerySlideShow#repeatEnd start");
//        isSlideRepeat = false;
//        isCheckRepeat = false;
//        Log.d("maguro", "GallerySlideShow#repeatEnd end");
//    }
//
//    private void applyRotation(Bitmap bitmap) {
//        Log.d("maguro", "GallerySlideShow#applyRotation start");
//        if (mImageViewEven.getVisibility() == View.GONE) {
//            Log.d("maguro", "GallerySlideShow#applyRotation mImageViewEven");
//            mImageViewEven.setImageBitmap(bitmap);
//            applyRotation(mContainer, 0f, 90f, 180f, 0f);
//        } else {
//            Log.d("maguro", "GallerySlideShow#applyRotation mImageViewOdd");
//            mImageViewOdd.setImageBitmap(bitmap);
//            // XXX 勝手に適当にかえてみた
//            // applyRotation(mContainer, 180f, 270f, 360f, 0f);
//            applyRotation(mContainer, 0f, 270f, 360f, 0f);
//        }
//        Log.d("maguro", "GallerySlideShow#applyRotation end");
//    }
//
//    private void applyRotation(ViewGroup view, float start, float mid, float end, float depth) {
//        Log.d("maguro", "GallerySlideShow#applyRotation start");
//        this.centerX = view.getWidth() / 2.0f;
//        this.centerY = view.getHeight() / 2.0f;
//        Rotate3dAnimation rot = new Rotate3dAnimation(start, mid, centerX, centerY, depth, true);
//        rot.setDuration(DURATION);
//        rot.setAnimationListener(new DisplayNextView(mid, end, depth));
//        view.startAnimation(rot);
//        Log.d("maguro", "GallerySlideShow#applyRotation end");
//    }
//
//    public class DisplayNextView implements AnimationListener {
//        private float mid;
//        private float end;
//        private float depth;
//
//        public DisplayNextView(float mid, float end, float depth) {
//            Log.d("maguro", "GallerySlideShow#DisplayNextView");
//            this.mid = mid;
//            this.end = end;
//            this.depth = depth;
//        }
//
//        @Override
//        public void onAnimationEnd(Animation animation) {
//            Log.d("maguro", "GallerySlideShow#onAnimationEnd start");
//            mContainer.post(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("maguro", "GallerySlideShow#onAnimationEnd mContatiner start");
//                    if (mImageViewEven.getVisibility() == View.GONE) {
//                        mImageViewEven.setVisibility(View.VISIBLE);
//                        mImageViewDefault.setVisibility(View.GONE);
//                        mImageViewOdd.setVisibility(View.GONE);
//                    } else {
//                        mImageViewEven.setVisibility(View.GONE);
//                        mImageViewDefault.setVisibility(View.GONE);
//                        mImageViewOdd.setVisibility(View.VISIBLE);
//                    }
//                    // XXX ここが変な反転させている所か？？？
//                    Rotate3dAnimation rot = new Rotate3dAnimation(mid, end, centerX, centerY, depth, false);
//                    rot.setDuration(DURATION);
//                    rot.setInterpolator(new AccelerateInterpolator());
//                    mContainer.startAnimation(rot);
//                    Log.d("maguro", "GallerySlideShow#onAnimationEnd mContatiner end");
//                }
//            });
//            Log.d("maguro", "GallerySlideShow#onAnimationEnd end");
//        }
//
//        @Override
//        public void onAnimationRepeat(Animation animation) {
//            Log.d("maguro", "GallerySlideShow#onAnimationRepeat");
//        }
//
//        @Override
//        public void onAnimationStart(Animation animation) {
//            Log.d("maguro", "GallerySlideShow#onAnimationStart");
//        }
//    }
//
//    private void setupViewMail(MessageBean message) {
//        Log.d("maguro", "GallerySlideShow#setupViewMail start");
//        mMailSubject.setText(message.getSubject());
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd h:mm a");
//        mMailDate.setText(sdf.format(message.getDate()));
//        if (message.isFlagAnswered()) {
//            mAnswered.setVisibility(View.VISIBLE);
//        }
//        Log.d("maguro", "GallerySlideShow#setupViewMail end");
//    }
//
//    private void synchronizeMailbox(Account account, String folderName) {
//        Log.d("maguro", "GallerySlideShow#synchronizeMailbox start");
//        Folder remoteFolder = null;
//        LocalFolder tLocalFolder = null;
//
//        /*
//         * We don't ever sync the Outbox or errors folder
//         */
//        if (folderName.equals(account.getOutboxFolderName())
//            || folderName.equals(account.getErrorFolderName())) {
//            return;
//        }
//        try {
//            /*
//             * Get the message list from the local store and create an index of
//             * the uids within the list.
//             */
//            final LocalStore localStore = account.getLocalStore();
//            tLocalFolder = localStore.getFolder(folderName);
//            final LocalFolder localFolder = tLocalFolder;
//            localFolder.open(OpenMode.READ_WRITE);
//            localFolder.updateLastUid();
//            Message[] localMessages = localFolder.getMessages(null);
//            HashMap<String, Message> localUidMap = new HashMap<String, Message>();
//            for (Message message : localMessages) {
//                localUidMap.put(message.getUid(), message);
//            }
//            Store remoteStore = account.getRemoteStore();
//            remoteFolder = remoteStore.getFolder(folderName);
//            /*
//             * Open the remote folder. This pre-loads certain metadata like
//             * message count.
//             */
//            remoteFolder.open(OpenMode.READ_WRITE);
//            if (Account.EXPUNGE_ON_POLL.equals(account.getExpungePolicy())) {
//                remoteFolder.expunge();
//            }
//            /*
//             * Get the remote message count.
//             */
//            int remoteMessageCount = remoteFolder.getMessageCount();
//            int visibleLimit = localFolder.getVisibleLimit();
//            if (visibleLimit < 0) {
//                visibleLimit = RakuPhotoMail.DEFAULT_VISIBLE_LIMIT;
//            }
//            Message[] remoteMessageArray = new Message[0];
//            final ArrayList<Message> remoteMessages = new ArrayList<Message>();
//            HashMap<String, Message> remoteUidMap = new HashMap<String, Message>();
//            final Date earliestDate = account.getEarliestPollDate();
//            if (remoteMessageCount > 0) {
//                /* Message numbers start at 1. */
//                int remoteStart;
//                if (visibleLimit > 0) {
//                    remoteStart = Math.max(0, remoteMessageCount - visibleLimit) + 1;
//                } else {
//                    remoteStart = 1;
//                }
//                int remoteEnd = remoteMessageCount;
//                final AtomicInteger headerProgress = new AtomicInteger(0);
//                remoteMessageArray = remoteFolder.getMessages(remoteStart, remoteEnd, earliestDate, null);
//                for (Message thisMess : remoteMessageArray) {
//                    headerProgress.incrementAndGet();
//                    Message localMessage = localUidMap.get(thisMess.getUid());
//                    if (localMessage == null || !localMessage.olderThan(earliestDate)) {
//                        remoteMessages.add(thisMess);
//                        remoteUidMap.put(thisMess.getUid(), thisMess);
//                    }
//                }
//                remoteMessageArray = null;
//            } else if (remoteMessageCount < 0) {
//                throw new Exception("Message count " + remoteMessageCount + " for folder " + folderName);
//            }
//            /*
//             * Remove any messages that are in the local store but no longer on
//             * the remote store or are too old
//             */
//            if (account.syncRemoteDeletions()) {
//                ArrayList<Message> destroyMessages = new ArrayList<Message>();
//                for (Message localMessage : localMessages) {
//                    if (remoteUidMap.get(localMessage.getUid()) == null) {
//                        destroyMessages.add(localMessage);
//                    }
//                }
//                localFolder.destroyMessages(destroyMessages.toArray(new Message[0]));
//            }
//            localMessages = null;
//            setLocalFlaggedCountToRemote(localFolder, remoteFolder);
//            /* Notify listeners that we're finally done. */
//            localFolder.setLastChecked(System.currentTimeMillis());
//            localFolder.setStatus(null);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            closeFolder(remoteFolder);
//            closeFolder(tLocalFolder);
//        }
//        Log.d("maguro", "GallerySlideShow#syncHronizeMailbox end");
//    }
//
//    private void setLocalFlaggedCountToRemote(LocalFolder localFolder, Folder remoteFolder)
//                    throws MessagingException {
//        Log.d("maguro", "GallerySlideShow#setLocalFlaggedCountToRemote start");
//        int remoteFlaggedMessageCount = remoteFolder.getFlaggedMessageCount();
//        if (remoteFlaggedMessageCount != -1) {
//            localFolder.setFlaggedMessageCount(remoteFlaggedMessageCount);
//        } else {
//            int flaggedCount = 0;
//            Message[] messages = localFolder.getMessages(null, false);
//            for (Message message : messages) {
//                if (message.isSet(Flag.FLAGGED) && !message.isSet(Flag.DELETED)) {
//                    flaggedCount++;
//                }
//            }
//            localFolder.setFlaggedMessageCount(flaggedCount);
//        }
//        Log.d("maguro", "GallerySlideShow#setLocalFlaggedCountToRemote end");
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.gallery_mail_slide:
//                try {
//                    onSlide();
//                } catch (MessagingException e) {
//                    e.printStackTrace();
//                    Log.e("rakuphotomail", "Hello!Hello!Error! HAHAHAHA!", e);
//                }
//                break;
//            case R.id.gallery_mail_reply:
//                onReply();
//                break;
//            case R.id.gallery_mail_pre:
//                onMailPre();
//                break;
//            case R.id.gallery_mail_next:
//                onMailNext();
//                break;
//            case R.id.gallery_attachment_picuture_default:
//                break;
//            case R.id.gallery_attachment_picuture_even:
//                // XXX りふぁくたりんぐしたい
//                try {
//                    onSlideStop();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case R.id.gallery_attachment_picuture_odd:
//                // XXX りふぁくたりんぐしたい
//                try {
//                    onSlideStop();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                break;
//            default:
//                Log.w(RakuPhotoMail.LOG_TAG, "onClick is no Action !!!!");
//        }
//    }
//
//    private void onMailCurrent() {
//        Log.d("maguro", "GallerySlideShow#onMailCurrent start");
//        int currentIndex = mMessageUids.indexOf(mMessageUid);
//        int maxIndex = mMessageUids.size() - 1;
//        int minIndex = 0;
//        if (currentIndex >= minIndex && currentIndex <= maxIndex) {
//            setMailDisp(currentIndex);
//        } else {
//            Toast.makeText(GallerySlideShowBkup.this, "メールが存在しません。", Toast.LENGTH_SHORT);
//            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#onMailCurrent() メールが存在しません");
//        }
//        Log.d("maguro", "GallerySlideShow#onMailCurrent end");
//    }
//
//    private void onMailPre() {
//        Log.d("maguro", "GallerySlideShow#onMailPre start");
//        int preIndex = mMessageUids.indexOf(mMessageUid) + 1;
//        int maxIndex = mMessageUids.size() - 1;
//        if (preIndex <= maxIndex) {
//            setMailDisp(preIndex);
//            if (preIndex == maxIndex) {
//                mMailPre.setVisibility(View.GONE);
//                mMailSeparator1.setVisibility(View.GONE);
//            }
//        } else {
//            Toast.makeText(GallerySlideShowBkup.this, "メールが存在しません。", Toast.LENGTH_SHORT);
//            mMailPre.setVisibility(View.GONE);
//            mMailSeparator1.setVisibility(View.GONE);
//        }
//        Log.d("maguro", "GallerySlideShow#onMailPre end");
//    }
//
//    private void onMailNext() {
//        Log.d("maguro", "GallerySlideShow#onMailNext start");
//        int nextIndex = mMessageUids.indexOf(mMessageUid) - 1;
//        int minIndex = 0;
//        if (nextIndex >= minIndex) {
//            setMailDisp(nextIndex);
//            if (nextIndex == minIndex) {
//                mMailNext.setVisibility(View.GONE);
//                mMailSeparator3.setVisibility(View.GONE);
//            }
//        } else {
//            Toast.makeText(GallerySlideShowBkup.this, "メールが存在しません。", Toast.LENGTH_SHORT);
//            mMailNext.setVisibility(View.GONE);
//            mMailSeparator3.setVisibility(View.GONE);
//        }
//        Log.d("maguro", "GallerySlideShow#onMailNext end");
//    }
//
//    private void setMailDisp(int index) {
//        Log.d("maguro", "GallerySlideShow#setMailDisp start");
//        setContentView(R.layout.gallery_slide_show_stop);
//        setupSlideStopViews();
//        mMessageUid = mMessageUids.get(index);
//        MessageBean message = mMessages.get(mMessageUid);
//        CopyOnWriteArrayList<AttachmentBean> attachments = message.getAttachments();
//        Bitmap bitmap = populateFromPart(attachments.get(0).getPart());
//        if (bitmap == null) {
//            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#setMailDisp bitmap is null");
//            return;
//        }
//        mImageViewPicture.setImageBitmap(bitmap);
//        if (attachments.size() > 1) {
//            ArrayList<Bitmap> list = new ArrayList<Bitmap>();
//            for (AttachmentBean bean : attachments) {
//                list.add(populateFromPartThumbnail(bean.getPart()));
//            }
//            mImageAdapter = new ImageAdapter(mContext);
//            mImageAdapter.setImageItems(list);
//            mGallery.setAdapter(mImageAdapter);
//            mGallery.setOnItemClickListener((OnItemClickListener) mContext);
//        } else {
//            mGalleryLinearLayout.setVisibility(View.GONE);
//        }
//        int maxIndex = mMessageUids.size() - 1;
//        int minIndex = 0;
//        if (minIndex == index) {
//            mMailNext.setVisibility(View.GONE);
//            mMailSeparator3.setVisibility(View.GONE);
//        } else if (maxIndex == index) {
//            mMailPre.setVisibility(View.GONE);
//            mMailSeparator1.setVisibility(View.GONE);
//        } else {
//            // XXX none
//            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideShow#setMailDisp index(" + index + ")undefind.");
//        }
//        setupViewMail(message);
//        Log.d("maguro", "GallerySlideShow#setMailDisp end");
//    }
//
//    private void onSlide() throws MessagingException {
//        Log.d("maguro", "GallerySlideShow#onSlide start");
//        if (null == mMessageUid || "".equals(mMessageUid)) {
//            mMessageUid = messageBean.getUid();
//        }
//        setContentView(R.layout.gallery_slide_show);
//        setupSlideShowViews();
//        mImageViewDefault.setVisibility(View.GONE);
//        List<MessageInfo> messageInfoList = getMessages();
//        Log.d("maguro", "GallerySlideShow#onSlide messageInfoList.size():" + messageInfoList.size());
//        if (messageInfoList.size() > 0) {
//            setSlideInfo(messageInfoList);
//            if (!messageSlideThread.isAlive()) {
//                isSlideRepeat = true;
//                restartMesasgeSlideThread = new Thread(messageSlide);
//                restartMesasgeSlideThread.start();
//            }
//            // if (null == checkMailThread) {
//            // isCheckRepeat = true;
//            // restartCheckMailThread = new Thread(checkMail);
//            // restartCheckMailThread.start();
//            // }
//        } else {
//            Log.d("maguro", "GallerySlideShow#onSlide スライドショーできねーよ");
//            onStop();
//        }
//        Log.d("maguro", "GallerySlideShow#onSlide end");
//    }
//
//    private void onSlideStop() throws InterruptedException {
//        Log.d("maguro", "GallerySlideShow#onSlideStop start");
//        Log.d("maguro", "GallerySlideShow#onSlideStop isSlideRepeat:" + isSlideRepeat);
//        if (isSlideRepeat) {
//            repeatEnd();
//            if (null != createMessageListThread && createMessageListThread.isAlive()) {
//                Log.d("maguro", "onSlideStop createMessageListThread.join() start");
//                createMessageListThread.join();
//                Log.d("maguro", "onSlideStop createMessageListThread.join() end");
//            }
//            if (null != messageSlideThread && messageSlideThread.isAlive()) {
//                messageSlideThread.join();
//            }
//            // XXX Checkhはスレッドすんのやめる予定
//            // if (null != checkMailThread && checkMailThread.isAlive()) {
//            // checkMailThread.join();
//            // }
//            if (null != restartMesasgeSlideThread && restartMesasgeSlideThread.isAlive()) {
//                restartMesasgeSlideThread.join();
//            }
//            // XXX Checkhはスレッドすんのやめる予定
//            // if (null != restartCheckMailThread &&
//            // restartCheckMailThread.isAlive()) {
//            // restartCheckMailThread.join();
//            // }
//            onMailCurrent();
//        }
//        Log.d("maguro", "GallerySlideShow#onSlideStop end");
//    }
//
//    private void onReply() {
//        Log.d("maguro", "GallerySlideShow#onReply start");
//        MessageBean message = mMessages.get(mMessageUid);
//        if (null != message) {
//            GallerySendingMail.actionReply(this, mMessages.get(mMessageUid));
//        } else {
//            Toast.makeText(GallerySlideShowBkup.this, "メールが存在しません。", Toast.LENGTH_SHORT);
//            Log.w("rakuphoto", "GallerySlideShow#onReply() メールが存在しません UID:" + mMessageUid);
//        }
//        Log.d("maguro", "GallerySlideShow#onReply end");
//    }
//
//    public class ImageAdapter extends BaseAdapter {
//        private int mGalleryItemBackground;
//        private Context mContext;
//        private ArrayList<Bitmap> mImageItems = new ArrayList<Bitmap>();
//
//        public ImageAdapter(Context c) {
//            Log.d("maguro", "ImageAdapter#ImageAdapter start");
//            mContext = c;
//            TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
//            mGalleryItemBackground = a
//                .getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
//            a.recycle();
//
//            mImageItems = setDroidList();
//            Log.d("maguro", "ImageAdapter#ImageAdapter end");
//        }
//
//        public int getCount() {
//            Log.d("maguro", "ImageAdapter#getCount");
//            return mImageItems.size();
//        }
//
//        public Object getItem(int position) {
//            Log.d("maguro", "ImageAdapter#getItem");
//            return mImageItems.get(position);
//        }
//
//        public long getItemId(int position) {
//            Log.d("maguro", "ImageAdapter#getItemId");
//            return position;
//        }
//
//        public View getView(int position, View convertView, ViewGroup parent) {
//            Log.d("maguro", "ImageAdapter#getItemView");
//            ImageView i = new ImageView(mContext);
//            Bitmap bitmap = (Bitmap) getItem(position);
//            i.setImageBitmap(bitmap);
//            i.setBackgroundResource(mGalleryItemBackground);
//
//            return i;
//        }
//
//        public void setImageItems(ArrayList<Bitmap> imageItems) {
//            Log.d("maguro", "ImageAdapter#setImageItems");
//            this.mImageItems = imageItems;
//        }
//
//        public ArrayList<Bitmap> getImageItems() {
//            Log.d("maguro", "ImageAdapter#getImageItems");
//            return this.mImageItems;
//        }
//
//        // XXX まさにゴミ
//        private ArrayList<Bitmap> setDroidList() {
//            Log.d("maguro", "ImageAdapter#setDroidList");
//            return new ArrayList<Bitmap>();
//        }
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
//        Log.d("maguro", "GallerySlideShow#onItemClick start");
//        Toast.makeText(GallerySlideShowBkup.this, "" + position, Toast.LENGTH_LONG).show();
//        if (newMailFlg) {
//            Log.d("maguro", "GallerySlideShow#onItemClick 新着メール対応");
//            mImageViewPicture.setImageBitmap(populateFromPart(newAttachmentList.get(position).getPart()));
//        } else {
//            Log.d("maguro", "GallerySlideShow#onItemClick 既存メール対応");
//            MessageBean message = mMessages.get(mMessageUid);
//            CopyOnWriteArrayList<AttachmentBean> list = message.getAttachments();
//            mImageViewPicture.setImageBitmap(populateFromPart(list.get(position).getPart()));
//        }
//        Log.d("maguro", "GallerySlideShow#onItemClick end");
//    }
}
