/*
 * Copyright (c) 2011, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.setup.AccountSettings;
import jp.co.fttx.rakuphotomail.rakuraku.bean.AttachmentBean;
import jp.co.fttx.rakuphotomail.rakuraku.bean.MessageBean;
import jp.co.fttx.rakuphotomail.rakuraku.exception.RakuRakuException;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideAttachment;
import jp.co.fttx.rakuphotomail.rakuraku.photomail.SlideMessage;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;
import jp.co.fttx.rakuphotomail.rakuraku.util.ThumbnailImageAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author tooru.oguri
 * @since rakuphoto 0.1-beta1
 */
public class GallerySlideStop extends RakuPhotoActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    /**
     * context
     */
    private Context mContext;
    /**
     * Intent get/put account uuid
     */
    private static final String EXTRA_ACCOUNT = "account";
    /**
     * Intent get/put folder name
     */
    private static final String EXTRA_FOLDER = "folder";
    /**
     * Intent get/put message uid
     */
    private static final String EXTRA_UID = "uid";
    /**
     * Display Receive Date
     */
    private static final String DATE_FORMAT = "yyyy/MM/dd h:mm a";
    /**
     * R.id.gallery_mail_slide
     */
    private static final int ID_GALLERY_MAIL_SLIDE = R.id.gallery_mail_slide;
    /**
     * R.id.gallery_mail_reply
     */
    private static final int ID_GALLERY_MAIL_REPLY = R.id.gallery_mail_reply;
    /**
     * R.id.gallery_mail_pre
     */
    private static final int ID_GALLERY_MAIL_PRE = R.id.gallery_mail_pre;
    /**
     * R.id.gallery_mail_next
     */
    private static final int ID_GALLERY_MAIL_NEXT = R.id.gallery_mail_next;
    /**
     * main image
     */
    private ImageView mImageViewPicture;
    /**
     *
     */
    private LinearLayout mMailSubjectLayout;
    /**
     * mail subject
     */
    private TextView mMailSubject;
    /**
     * mail subject
     */
    private String mSubject;
    /**
     * mail receive date
     */
    private TextView mMailDate;
    /**
     * mail sent flag
     */
    private TextView mAnswered;
    /**
     * mail sent flag
     */
    private ImageView mAnsweredMark;
    /**
     * mail pre disp
     */
    private Button mMailPre;
    /**
     * re slide
     */
    private Button mMailSlide;
    /**
     * mail next disp
     */
    private Button mMailNext;
    /**
     * mail reply
     */
    private Button mMailReply;
    /**
     * user account
     */
    private Account mAccount;
    /**
     * folder name
     */
    private String mFolder;
    /**
     * message uid
     */
    private String mUid;
    /**
     *
     */
    private MessageBean mMessageBean;
    /**
     * isBound
     */
    private boolean mIsBound = false;
    /**
     * thumbnail
     */
    private LinearLayout mGalleryThumbnailLayout;
    /**
     * thumbnail
     */
    private Gallery mGalleryThumbnail;
    /**
     * thumbnail
     */
    private boolean isThumbnail;
    /**
     * slide target attachment list
     */
    private ArrayList<AttachmentBean> mSlideTargetAttachmentList = new ArrayList<AttachmentBean>();
    /**
     *
     */
    private boolean isDispMailLayout = true;
    /**
     * view subject
     */
    private TextView mSenderName;
    /**
     *
     */
    PopupWindow pWindowMailDetail;
    /**
     *
     */
    boolean mMessagePopup = false;

    /**
     * @param context context
     * @param account account info
     * @param folder  receive mail folder name
     * @param uid     message uid
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    public static void actionHandle(Context context, Account account, String folder, String uid) {
        Intent intent = new Intent(context, GallerySlideStop.class);
        if (null == account || null == folder || uid == null) {
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideStop#actionHandle account:" + account + " folder:" + folder + " uid:" + uid);
            return;
        }
        intent.putExtra(EXTRA_ACCOUNT, account.getUuid());
        intent.putExtra(EXTRA_FOLDER, folder);
        intent.putExtra(EXTRA_UID, uid);
        context.startActivity(intent);
    }

    /**
     * @param outState bundle
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        outState.putString(EXTRA_FOLDER, mFolder);
        outState.putString(EXTRA_UID, mUid);
    }

    /**
     * @param savedInstanceState save
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAccount = Preferences.getPreferences(this)
                .getAccount(savedInstanceState.getString(EXTRA_ACCOUNT));
        mFolder = savedInstanceState.getString(EXTRA_FOLDER);
        mUid = savedInstanceState.getString(EXTRA_UID);
    }

    /**
     * @param savedInstanceState save
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gallery_slide_show_postcard1_stop);
        setupViews();
        onNewIntent(getIntent());
        setMailMoveVisibility(mUid);
        onDispMail();
    }

    private void setupViews() {
        mImageViewPicture = (ImageView) findViewById(R.id.gallery_mail_picture);
        mImageViewPicture.setVisibility(View.VISIBLE);
        setupViewTopMailInfo();
        setupViewBottomButton();
        setupViewGalleryThumbnail();
    }

    private void setupViewTopMailInfo() {
        mMailSubjectLayout = (LinearLayout) findViewById(R.id.gallery_mail_subject_layout);
        mMailSubjectLayout.setOnClickListener(this);
        mMailSubject = (TextView) findViewById(R.id.gallery_mail_subject);
        mMailDate = (TextView) findViewById(R.id.gallery_mail_date);
        mSenderName = (TextView) findViewById(R.id.gallery_mail_sender_name);
        mAnswered = (TextView) findViewById(R.id.gallery_mail_sent_flag);
        mAnswered.setVisibility(View.GONE);
        mAnsweredMark = (ImageView) findViewById(R.id.gallery_mail_sent_flag_mark);
        mAnsweredMark.setVisibility(View.GONE);
    }

    private void setupViewBottomButton() {
        mMailPre = (Button) findViewById(ID_GALLERY_MAIL_PRE);
        mMailPre.setOnClickListener(this);
        mMailSlide = (Button) findViewById(ID_GALLERY_MAIL_SLIDE);
        mMailSlide.setOnClickListener(this);
        mMailReply = (Button) findViewById(ID_GALLERY_MAIL_REPLY);
        mMailReply.setOnClickListener(this);
        mMailNext = (Button) findViewById(ID_GALLERY_MAIL_NEXT);
        mMailNext.setOnClickListener(this);
    }

    private void setupViewGalleryThumbnail() {
        mGalleryThumbnailLayout = (LinearLayout) findViewById(R.id.gallery_thumbnail_layout);
        mGalleryThumbnailLayout.setVisibility(View.GONE);
        mGalleryThumbnail = (Gallery) findViewById(R.id.gallery_thumbnail);
        mGalleryThumbnail.setOnItemClickListener((AdapterView.OnItemClickListener) mContext);
        isThumbnail = false;
    }

    /**
     * @param intent intent
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    @Override
    public void onNewIntent(Intent intent) {
        mAccount = Preferences.getPreferences(this).getAccount(intent.getStringExtra(EXTRA_ACCOUNT));
        mFolder = intent.getStringExtra(EXTRA_FOLDER);
        mUid = intent.getStringExtra(EXTRA_UID);
    }

    private ArrayList<Bitmap> makeBitmapList(ArrayList<AttachmentBean> beanArrayList) {
        ArrayList<Bitmap> resultList = new ArrayList<Bitmap>();
        for (AttachmentBean attachmentBean : beanArrayList) {
            resultList.add(getThumbnailBitmap(attachmentBean));
        }
        return resultList;
    }

    private Bitmap getThumbnailBitmap(AttachmentBean attachmentBean) {
        return SlideAttachment.getThumbnailBitmap(getApplicationContext(), mAccount, attachmentBean);
    }

    private void setImageViewPicture(ArrayList<AttachmentBean> attachmentBeanList, int index) {
        Bitmap bitmap = null;
        try {
            bitmap = SlideAttachment.getBitmap(getApplicationContext(), getWindowManager().getDefaultDisplay(), mAccount, attachmentBeanList.get(index));
            mImageViewPicture.setImageBitmap(bitmap);
        } catch (RakuRakuException e) {
            onAlertNoImage();
        }
    }

    private void setDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        mMailDate.setText(sdf.format(date));
    }

    private void setAnswered(boolean isFlagAnswered) {
        if (isFlagAnswered) {
            mAnswered.setVisibility(View.VISIBLE);
            mAnsweredMark.setVisibility(View.VISIBLE);
        } else {
            mAnswered.setVisibility(View.GONE);
            mAnsweredMark.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case ID_GALLERY_MAIL_SLIDE:
                onSlide();
                break;
            case ID_GALLERY_MAIL_REPLY:
                onReply();
                break;
            case ID_GALLERY_MAIL_PRE:
                onMailPre();
                break;
            case ID_GALLERY_MAIL_NEXT:
                onMailNext();
                break;
            case R.id.gallery_mail_subject_layout:
                mMailSubjectLayout.setEnabled(false);
                onMessageDetail();
                break;
            default:
                Log.w(RakuPhotoMail.LOG_TAG, "onClick is no Action !!!!");
        }
    }

    private void onReply() {
        if (null != mMessageBean) {
            GallerySendingMail.actionReply(this, mMessageBean);
        } else {
            Toast.makeText(GallerySlideStop.this, "メールが存在しません。", Toast.LENGTH_SHORT);
            Log.w(RakuPhotoMail.LOG_TAG, "GallerySlideStop#onReply() メールが存在しません UID:" + mUid);
        }
    }

    private void setViewSlide() {
        isDispMailLayout = true;
        mSlideTargetAttachmentList = SlideAttachment.getSlideTargetList(mMessageBean.getAttachmentBeanList());
        //Thumbnail
        ArrayList<AttachmentBean> attachmentBeanList = mSlideTargetAttachmentList;
        if (1 < attachmentBeanList.size()) {
            mGalleryThumbnailLayout.setVisibility(View.VISIBLE);
            ThumbnailImageAdapter thumbnailAdapter = new ThumbnailImageAdapter(getApplicationContext());
            thumbnailAdapter.setImageItems(makeBitmapList(attachmentBeanList));
            mGalleryThumbnail.setAdapter(thumbnailAdapter);
            isThumbnail = true;
        } else {
            mGalleryThumbnailLayout.setVisibility(View.GONE);
            isThumbnail = false;
        }

        setImageViewPicture(mSlideTargetAttachmentList, 0);
        //TODO 140文字に制限します(config)
        mSubject = RakuPhotoStringUtils.limitMessage(mMessageBean.getSubject(), 140);
        mMailSubject.setText(mSubject);
        mSenderName.setText(mMessageBean.getSenderName().trim());
        setDate(mMessageBean.getDate());
        setAnswered(mMessageBean.isFlagAnswered());
    }

    /**
     * @param messageBean MessageBean
     * @throws RakuRakuException exception
     * @author tooru.oguri
     * @since rakuphoto 0.1-beta1
     */
    private void onDisp(MessageBean messageBean) {
        mMessageBean = messageBean;
        setViewSlide();
    }

    private void onMessageDetail() {
        showPopupWindow(getApplicationContext(), this.findViewById(R.id.gallery_mail_subject_layout));
    }

    private void showPopupWindow(Context context, View parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View vPopupWindow = inflater.inflate(R.layout.gallery_message_detail_popup, null, false);
        Display display = getWindowManager().getDefaultDisplay();
        pWindowMailDetail = new PopupWindow(vPopupWindow, display.getWidth() / 2, display.getHeight() / 2, true);
        setupPopupWindowViews(vPopupWindow);
        pWindowMailDetail.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    private void setupPopupWindowViews(View vPopupWindow) {
        TextView subject = (TextView) vPopupWindow.findViewById(R.id.gallery_message_detail);
        subject.setText(mSubject);
        TextView close = (TextView) vPopupWindow.findViewById(R.id.gallery_message_detail_popup_close);
        /* 閉じる */
        close.setOnClickListener(new PopupClickEvent());
    }

    private void onMailInfoClose() {
        mMessagePopup = false;
        pWindowMailDetail.dismiss();
    }

    private class PopupClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.gallery_message_detail_popup_close:
                    mMailSubjectLayout.setEnabled(true);
                    onMailInfoClose();
                    break;
                default:
                    Log.w(RakuPhotoMail.LOG_TAG, "PopupClickEvent#onClick is no Action !!!!");
            }
        }
    }

    private void setMailMoveVisibility(String uid) {
        if (!SlideMessage.isNextMessage(mAccount, mFolder, uid)) {
            mMailNext.setEnabled(false);
        } else {
            mMailNext.setEnabled(true);
        }
        if (!SlideMessage.isPreMessage(mAccount, mFolder, uid)) {
            mMailPre.setEnabled(false);
        } else {
            mMailPre.setEnabled(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMessagePopup = true;
        setImageViewPicture(mSlideTargetAttachmentList, (int) id);
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onMailPre() {
        new DispPreMailTask(this).execute();
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onMailNext() {
        new DispNextMailTask(this).execute();
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onSlide() {
        new DispSlideStartTask(this).execute();
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private void onDispMail() {
        new DownloadAttachmentTask(this).execute();
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class DispPreMailTask extends AsyncTask<Void, Integer, MessageBean> implements DialogInterface.OnCancelListener {
        ProgressDialog dialog;
        Context context;

        public DispPreMailTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please wait");
            dialog.setMessage("表示中のメールより１件古いメールを表示中です。\nしばらくお待ちください。");
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
        protected MessageBean doInBackground(Void... params) {
            MessageBean messageBean = new MessageBean();
            try {
                publishProgress(10);
                SlideAttachment.downloadAttachment(mAccount, mFolder, SlideMessage.getPreUid(mAccount, mFolder, mMessageBean.getUid()));
                publishProgress(30);
                messageBean = SlideMessage.getPreMessage(mAccount, mFolder, mMessageBean.getUid());
                publishProgress(60);
            } catch (RakuRakuException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "DispMailPreTask#doInBackground() 前のメールが取得できず UID:" + mMessageBean.getUid());
            }
            return messageBean;
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
        protected void onPostExecute(MessageBean messageBean) {
            onDisp(messageBean);
            publishProgress(80);
            setMailMoveVisibility(messageBean.getUid());
            publishProgress(100);
            onCancelled();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class DispNextMailTask extends AsyncTask<Void, Integer, MessageBean> implements DialogInterface.OnCancelListener {
        ProgressDialog dialog;
        Context context;

        public DispNextMailTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please wait");
            dialog.setMessage("表示中のメールより１件新しいメールを表示中です。\nしばらくお待ちください。");
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
        protected MessageBean doInBackground(Void... params) {
            MessageBean messageBean = new MessageBean();
            try {
                publishProgress(10);
                SlideAttachment.downloadAttachment(mAccount, mFolder, SlideMessage.getNextUid(mAccount, mFolder, mMessageBean.getUid()));
                publishProgress(30);
                messageBean = SlideMessage.getNextMessage(mAccount, mFolder, mMessageBean.getUid());
                publishProgress(60);
            } catch (RakuRakuException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "DispMailPreTask#doInBackground() 次のメールが取得できず UID:" + mMessageBean.getUid());
            }
            return messageBean;
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
        protected void onPostExecute(MessageBean messageBean) {
            onDisp(messageBean);
            publishProgress(80);
            setMailMoveVisibility(messageBean.getUid());
            publishProgress(100);
            onCancelled();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class DispSlideStartTask extends AsyncTask<Void, Integer, Void> implements DialogInterface.OnCancelListener {
        ProgressDialog dialog;
        Context context;

        public DispSlideStartTask(Context context) {
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
            publishProgress(50);
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
            publishProgress(70);
            GallerySlideShow.actionSlideShow(context, mAccount, mFolder, mMessageBean.getUid());
            publishProgress(100);
            onCancelled();
            finish();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.message_list_option, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (isThumbnail) {
            menu.findItem(R.id.buttons_disabled).setVisible(true);
        } else {
            menu.findItem(R.id.buttons_disabled).setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.account_settings: {
                onEditAccount();
                return true;
            }
            case R.id.buttons_disabled: {
                onButtonsDisp();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void onEditAccount() {
        AccountSettings.actionSettings(this, mAccount);
    }

    private void onButtonsDisp() {
        if (isDispMailLayout) {
            isDispMailLayout = false;
            mGalleryThumbnailLayout.setVisibility(View.INVISIBLE);
        } else {
            isDispMailLayout = true;
            mGalleryThumbnailLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * @author tooru.oguri
     * @since 0.1-beta1
     */
    private class DownloadAttachmentTask extends AsyncTask<Void, Integer, Void> implements DialogInterface.OnCancelListener {
        ProgressDialog dialog;
        Context context;

        public DownloadAttachmentTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle("Please wait");
            dialog.setMessage("スライドショー情報をサーバーと同期中です。\nしばらくお待ちください。");
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
            publishProgress(20);
            SlideAttachment.downloadAttachment(mAccount, mFolder, mUid);
            publishProgress(40);
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
            try {
                mMessageBean = SlideMessage.getMessage(mAccount, mFolder, mUid);
                publishProgress(75);
            } catch (RakuRakuException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "EROOR:" + e.getMessage());
            }
            setViewSlide();
            publishProgress(100);
            onCancelled();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }
    }

    private void onAlertNoImage() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("らくフォトメール");
        alertDialogBuilder.setMessage("メモリリークにより、画像が表示できません。\n続行してよろしいでしょうか。\nなお同じ症状が頻発する場合は、設定により\n画像表示サイズを小さくすることをお勧めします。");
        alertDialogBuilder.setPositiveButton("はい、続行します",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton("いいえ、終了します",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
