package jp.co.fttx.rakuphotomail;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import jp.co.fttx.rakuphotomail.helper.Utility;
import jp.co.fttx.rakuphotomail.mail.Address;
import jp.co.fttx.rakuphotomail.mail.MessagingException;
import jp.co.fttx.rakuphotomail.mail.Store;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore;
import jp.co.fttx.rakuphotomail.mail.store.StorageManager;
import jp.co.fttx.rakuphotomail.rakuraku.util.RakuPhotoStringUtils;
import jp.co.fttx.rakuphotomail.view.ColorChip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Account implements BaseAccount {
    public static final String INBOX = "INBOX";
    public static final String OUTBOX = "OUTBOX";

    public static final String EXPUNGE_IMMEDIATELY = "EXPUNGE_IMMEDIATELY";
    public static final String EXPUNGE_MANUALLY = "EXPUNGE_MANUALLY";
    public static final String EXPUNGE_ON_POLL = "EXPUNGE_ON_POLL";

    public static final int DELETE_POLICY_NEVER = 0;
    public static final int DELETE_POLICY_7DAYS = 1;
    public static final int DELETE_POLICY_ON_DELETE = 2;
    public static final int DELETE_POLICY_MARK_AS_READ = 3;

    public static final String TYPE_WIFI = "WIFI";
    public static final String TYPE_MOBILE = "MOBILE";
    public static final String TYPE_OTHER = "OTHER";
    private static final String[] networkTypes = {TYPE_WIFI, TYPE_MOBILE, TYPE_OTHER};

    private static final MessageFormat DEFAULT_MESSAGE_FORMAT = MessageFormat.HTML;
    private static final QuoteStyle DEFAULT_QUOTE_STYLE = QuoteStyle.PREFIX;
    private static final String DEFAULT_QUOTE_PREFIX = ">";
    private static final boolean DEFAULT_QUOTED_TEXT_SHOWN = true;
    private static final boolean DEFAULT_REPLY_AFTER_QUOTE = false;

    private int mDeletePolicy;

    private final String mUuid;
    private String mStoreUri;

    private String mLocalStorageProviderId;
    private String mTransportUri;
    private String mDescription;
    private String mAlwaysBcc;
    private int mAutomaticCheckIntervalMinutes;
    private int mDisplayCount;
    private int mChipColor;
    private long mLastAutomaticCheckTime;
    private long mLatestOldMessageSeenTime;
    private boolean mNotifyNewMail;
    private boolean mNotifySelfNewMail;
    private String mInboxFolderName;
    private String mDraftsFolderName;
    private String mSentFolderName;
    private String mTrashFolderName;
    private String mArchiveFolderName;
    private String mSpamFolderName;
    private String mAutoExpandFolderName;
    private FolderMode mFolderDisplayMode;
    private FolderMode mFolderSyncMode;
    private FolderMode mFolderPushMode;
    private int mAccountNumber;
    private boolean mSaveAllHeaders;
    private boolean mPushPollOnConnect;
    private boolean mNotifySync;
    private boolean mIsSignatureBeforeQuotedText;
    private String mExpungePolicy = EXPUNGE_IMMEDIATELY;
    private int mMaxPushFolders;
    private int mIdleRefreshMinutes;
    private boolean goToUnreadMessageSearch;
    private boolean mNotificationShowsUnreadCount;
    private final Map<String, Boolean> compressionMap = new ConcurrentHashMap<String, Boolean>();
    private Searchable searchableFolders;
    private boolean subscribedFoldersOnly;
    private int maximumAutoDownloadMessageSize = 10;
    private boolean mRingNotified;
    private MessageFormat mMessageFormat;
    private QuoteStyle mQuoteStyle;
    private String mQuotePrefix;
    private boolean mDefaultQuotedTextShown;
    private boolean mReplyAfterQuote;
    private boolean mSyncRemoteDeletions;

    private int attachmentCacheLimitCount = 5; //5;
    private long slideSleepTimeDuration; //20000L;
    private long serverSyncTimeDuration; //180000L;
    private int scaleRatio; //1;

    /*仕様上、現在は変更不可*/
    private int messageLimitCountFromDb = 5; //(変更不可)
    private int messageLimitCountFromRemote = 3; // 0だと全件(変更不可)
    private long serverSyncInitStartTimeDuration = 180000L;//(変更不可)
    // 全件チェック時の開始地点
    private int checkStartId = 0;
    // 全件チェック時の終了地点
    private int checkEndId = 0;
    // 全件チェックFlag
    private boolean isAllSync = true;
    // 途中範囲チェックFlag
    private boolean isSync = false;
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm";
    // スリープモードオン／オフ
    private boolean canSleep = true;
    // スライドショー時の日付・差出人・件名表示 オン／オフ
    private boolean canDispSlideShowInfo = true;

    // アプリ起動時の最新UID
    private String appRunLatestUid;
    // アプリ起動時の日時
    private Date appRunLatestDate = new Date();
    // アプリ起動時の１世代前のUID
    private String oldAppRunLatestUid;
    // アプリ起動時の１世代前の日時
    private Date oldAppRunLatestDate = new Date();
    // 新着メールチェックの最新UID
    private String newMailCheckLatestUid;
    // 新着メールチェックの日時
    private Date newMailCheckLatestDate = new Date();
    // 新着メールチェックの１世代前のUID
    private String oldNewMailCheckLatestUid;
    // 新着メールチェックの１世代前の日時
    private Date oldNewMailCheckLatestDate = new Date();
    // 過去メールチェックの最新UID
    private String pastMailCheckLatestUid;
    // 過去メールチェックの日時
    private Date pastMailCheckLatestDate = new Date();
    // 過去メールチェックの１世代前のUID
    private String oldPastMailCheckLatestUid;
    // 過去メールチェックの１世代前の日時
    private Date oldPastMailCheckLatestDate = new Date();

    private String lastSelectedFolderName = null;

    private List<Identity> identities;

    private NotificationSetting mNotificationSetting = new NotificationSetting();

    public enum FolderMode {
        NONE, ALL, FIRST_CLASS, FIRST_AND_SECOND_CLASS, NOT_SECOND_CLASS
    }

    public enum ScrollButtons {
        NEVER, ALWAYS, KEYBOARD_AVAILABLE
    }

    public enum ShowPictures {
        NEVER, ALWAYS, ONLY_FROM_CONTACTS
    }

    public enum Searchable {
        ALL, DISPLAYABLE, NONE
    }

    public enum QuoteStyle {
        PREFIX, HEADER
    }

    public enum MessageFormat {
        TEXT, HTML
    }

    public void init() {
        // 全件チェック時の開始地点
        checkStartId = 0;
        // 全件チェック時の終了地点
        checkEndId = 0;
        // 全件チェックFlag
        isAllSync = true;
        // 途中範囲チェックFlag
        isSync = false;
        // アプリ起動時の最新UID
        appRunLatestUid = null;
        // アプリ起動時の日時
        appRunLatestDate = new Date();
        // アプリ起動時の１世代前のUID
        oldAppRunLatestUid = null;
        // アプリ起動時の１世代前の日時
        oldAppRunLatestDate = new Date();
        // 新着メールチェックの最新UID
        newMailCheckLatestUid = null;
        // 新着メールチェックの日時
        newMailCheckLatestDate = new Date();
        // 新着メールチェックの１世代前のUID
        oldNewMailCheckLatestUid = null;
        // 新着メールチェックの１世代前の日時
        oldNewMailCheckLatestDate = new Date();
        // 過去メールチェックの最新UID
        pastMailCheckLatestUid = null;
        // 過去メールチェックの日時
        pastMailCheckLatestDate = new Date();
        // 過去メールチェックの１世代前のUID
        oldPastMailCheckLatestUid = null;
        // 過去メールチェックの１世代前の日時
        oldPastMailCheckLatestDate = new Date();
    }

    protected Account(Context context) {
        mUuid = UUID.randomUUID().toString();
        mLocalStorageProviderId =
                StorageManager.getInstance(context).getDefaultProviderId();
        mAutomaticCheckIntervalMinutes = -1;
        mIdleRefreshMinutes = 24;
        mSaveAllHeaders = true;
        mPushPollOnConnect = true;
        mDisplayCount = RakuPhotoMail.DEFAULT_VISIBLE_LIMIT;
        mAccountNumber = -1;
        mNotifyNewMail = false;
        mNotifySync = false;
        mNotifySelfNewMail = false;
        mFolderDisplayMode = FolderMode.NOT_SECOND_CLASS;
        mFolderSyncMode = FolderMode.FIRST_CLASS;
        mFolderPushMode = FolderMode.FIRST_CLASS;
        mIsSignatureBeforeQuotedText = false;
        mExpungePolicy = EXPUNGE_IMMEDIATELY;
        mAutoExpandFolderName = INBOX;
        mInboxFolderName = INBOX;
        mMaxPushFolders = 10;
        mChipColor = (new Random()).nextInt(0xffffff) + 0xff000000;
        goToUnreadMessageSearch = false;
        mNotificationShowsUnreadCount = true;
        subscribedFoldersOnly = false;
        maximumAutoDownloadMessageSize = 32768;
        mMessageFormat = DEFAULT_MESSAGE_FORMAT;
        mQuoteStyle = DEFAULT_QUOTE_STYLE;
        mQuotePrefix = DEFAULT_QUOTE_PREFIX;
        mDefaultQuotedTextShown = DEFAULT_QUOTED_TEXT_SHOWN;
        mReplyAfterQuote = DEFAULT_REPLY_AFTER_QUOTE;
        mSyncRemoteDeletions = true;

        searchableFolders = Searchable.ALL;

        identities = new ArrayList<Identity>();

        Identity identity = new Identity();
        identity.setSignatureUse(true);
        identity.setSignature(context.getString(R.string.default_signature));
        identity.setDescription(context.getString(R.string.default_identity_description));
        identities.add(identity);

        mNotificationSetting = new NotificationSetting();
        mNotificationSetting.setVibrate(false);
        mNotificationSetting.setVibratePattern(0);
        mNotificationSetting.setVibrateTimes(5);
        mNotificationSetting.setRing(true);
        mNotificationSetting.setRingtone("content://settings/system/notification_sound");
        mNotificationSetting.setLedColor(mChipColor);
    }

    protected Account(Preferences preferences, String uuid) {
        this.mUuid = uuid;
        loadAccount(preferences);
    }

    /**
     * Load stored settings for this account.
     */
    private synchronized void loadAccount(Preferences preferences) {

        SharedPreferences prefs = preferences.getPreferences();

        mStoreUri = Utility.base64Decode(prefs.getString(mUuid + ".storeUri", null));
        mLocalStorageProviderId = prefs.getString(mUuid + ".localStorageProvider",
                StorageManager.getInstance(RakuPhotoMail.app).getDefaultProviderId());
        mTransportUri = Utility.base64Decode(prefs.getString(mUuid + ".transportUri", null));
        mDescription = prefs.getString(mUuid + ".description", null);
        mAlwaysBcc = prefs.getString(mUuid + ".alwaysBcc", mAlwaysBcc);
        mAutomaticCheckIntervalMinutes = prefs.getInt(mUuid + ".automaticCheckIntervalMinutes", -1);
        mIdleRefreshMinutes = prefs.getInt(mUuid + ".idleRefreshMinutes", 24);
        mSaveAllHeaders = prefs.getBoolean(mUuid + ".saveAllHeaders", true);
        mPushPollOnConnect = prefs.getBoolean(mUuid + ".pushPollOnConnect", true);
        mDisplayCount = prefs.getInt(mUuid + ".displayCount", RakuPhotoMail.DEFAULT_VISIBLE_LIMIT);
        if (mDisplayCount < 0) {
            mDisplayCount = RakuPhotoMail.DEFAULT_VISIBLE_LIMIT;
        }
        mLastAutomaticCheckTime = prefs.getLong(mUuid + ".lastAutomaticCheckTime", 0);
        mLatestOldMessageSeenTime = prefs.getLong(mUuid + ".latestOldMessageSeenTime", 0);
        mNotifyNewMail = prefs.getBoolean(mUuid + ".notifyNewMail", false);
        mNotifySelfNewMail = prefs.getBoolean(mUuid + ".notifySelfNewMail", true);
        mNotifySync = prefs.getBoolean(mUuid + ".notifyMailCheck", false);
        mDeletePolicy = prefs.getInt(mUuid + ".deletePolicy", 0);
        mInboxFolderName = prefs.getString(mUuid + ".inboxFolderName", INBOX);
        mDraftsFolderName = prefs.getString(mUuid + ".draftsFolderName", "Drafts");
        mSentFolderName = prefs.getString(mUuid + ".sentFolderName", "Sent");
        mTrashFolderName = prefs.getString(mUuid + ".trashFolderName", "Trash");
        mArchiveFolderName = prefs.getString(mUuid + ".archiveFolderName", "Archive");
        mSpamFolderName = prefs.getString(mUuid + ".spamFolderName", "Spam");
        mExpungePolicy = prefs.getString(mUuid + ".expungePolicy", EXPUNGE_IMMEDIATELY);
        mSyncRemoteDeletions = prefs.getBoolean(mUuid + ".syncRemoteDeletions", true);

        mMaxPushFolders = prefs.getInt(mUuid + ".maxPushFolders", 10);
        goToUnreadMessageSearch = prefs.getBoolean(mUuid + ".goToUnreadMessageSearch", false);
        mNotificationShowsUnreadCount = prefs.getBoolean(mUuid + ".notificationUnreadCount", true);
        subscribedFoldersOnly = prefs.getBoolean(mUuid + ".subscribedFoldersOnly", false);
        maximumAutoDownloadMessageSize =
                prefs.getInt(mUuid + ".maximumAutoDownloadMessageSize", 32768);
        mMessageFormat = MessageFormat
                .valueOf(prefs.getString(mUuid + ".messageFormat", DEFAULT_MESSAGE_FORMAT.name()));
        mQuoteStyle = QuoteStyle
                .valueOf(prefs.getString(mUuid + ".quoteStyle", DEFAULT_QUOTE_STYLE.name()));
        mQuotePrefix = prefs.getString(mUuid + ".quotePrefix", DEFAULT_QUOTE_PREFIX);
        mDefaultQuotedTextShown =
                prefs.getBoolean(mUuid + ".defaultQuotedTextShown", DEFAULT_QUOTED_TEXT_SHOWN);
        mReplyAfterQuote = prefs.getBoolean(mUuid + ".replyAfterQuote", DEFAULT_REPLY_AFTER_QUOTE);
        for (String type : networkTypes) {
            Boolean useCompression = prefs.getBoolean(mUuid + ".useCompression." + type,
                    true);
            compressionMap.put(type, useCompression);
        }

        mAutoExpandFolderName = prefs.getString(mUuid + ".autoExpandFolderName", INBOX);

        mAccountNumber = prefs.getInt(mUuid + ".accountNumber", 0);

        Random random = new Random((long) mAccountNumber + 4);

        mChipColor = prefs.getInt(mUuid + ".chipColor",
                (random.nextInt(0x70)) +
                        (random.nextInt(0x70) * 0xff) +
                        (random.nextInt(0x70) * 0xffff) +
                        0xff000000);

        attachmentCacheLimitCount = prefs.getInt(mUuid + ".attachmentCacheLimitCount", 5);
        slideSleepTimeDuration = prefs.getLong(mUuid + ".slideSleepTimeDuration", 20000L);
        serverSyncTimeDuration = prefs.getLong(mUuid + ".serverSyncTimeDuration", 180000L);
        scaleRatio = prefs.getInt(mUuid + ".scaleRatio", 1);
        checkStartId = prefs.getInt(mUuid + ".checkStartId", 0);
        checkEndId = prefs.getInt(mUuid + ".checkEndId", 0);
        isAllSync = prefs.getBoolean(mUuid + ".isAllSync", false);
        isSync = prefs.getBoolean(mUuid + ".isSync", false);
        appRunLatestUid = prefs.getString(mUuid + ".appRunLatestUid", null);
        newMailCheckLatestUid = prefs.getString(mUuid + ".newMailCheckLatestUid", null);
        pastMailCheckLatestUid = prefs.getString(mUuid + ".pastMailCheckLatestUid", null);
        oldAppRunLatestUid = prefs.getString(mUuid + ".oldAppRunLatestUid", null);
        oldNewMailCheckLatestUid = prefs.getString(mUuid + ".oldNewMailCheckLatestUid", null);
        oldPastMailCheckLatestUid = prefs.getString(mUuid + ".oldPastMailCheckLatestUid", null);
        canSleep = prefs.getBoolean(mUuid + ".canSleep", true);
        canDispSlideShowInfo = prefs.getBoolean(mUuid + ".canDispSlideShowInfo", true);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try {
            String tmpAppRunLatestDate = prefs.getString(mUuid + ".appRunLatestDate", null);
            if (RakuPhotoStringUtils.isNotBlank(tmpAppRunLatestDate)) {
                appRunLatestDate = new Date();
                appRunLatestDate = sdf.parse(tmpAppRunLatestDate);
            }
            String tmpNewMailCheckLatestDate = prefs.getString(mUuid + ".newMailCheckLatestDate", null);
            if (RakuPhotoStringUtils.isNotBlank(tmpNewMailCheckLatestDate)) {
                newMailCheckLatestDate = new Date();
                newMailCheckLatestDate = sdf.parse(tmpNewMailCheckLatestDate);
            }
            String tmpPastMailCheckLatestDate = prefs.getString(mUuid + ".pastMailCheckLatestDate", null);
            if (RakuPhotoStringUtils.isNotBlank(tmpPastMailCheckLatestDate)) {
                pastMailCheckLatestDate = new Date();
                pastMailCheckLatestDate = sdf.parse(tmpPastMailCheckLatestDate);
            }
            String tmpOldAppRunLatestDate = prefs.getString(mUuid + ".oldAppRunLatestDate", null);
            if (RakuPhotoStringUtils.isNotBlank(tmpOldAppRunLatestDate)) {
                oldAppRunLatestDate = new Date();
                oldAppRunLatestDate = sdf.parse(tmpOldAppRunLatestDate);
            }
            String tmpOldNewMailCheckLatestDate = prefs.getString(mUuid + ".oldNewMailCheckLatestDate", null);
            if (RakuPhotoStringUtils.isNotBlank(tmpOldNewMailCheckLatestDate)) {
                oldNewMailCheckLatestDate = new Date();
                oldNewMailCheckLatestDate = sdf.parse(tmpOldNewMailCheckLatestDate);
            }
            String tmpOldPastMailCheckLatestDate = prefs.getString(mUuid + ".oldPastMailCheckLatestDate", null);
            if (RakuPhotoStringUtils.isNotBlank(tmpOldPastMailCheckLatestDate)) {
                oldPastMailCheckLatestDate = new Date();
                oldPastMailCheckLatestDate = sdf.parse(tmpOldPastMailCheckLatestDate);
            }
        } catch (ParseException e) {
            Log.e(RakuPhotoMail.LOG_TAG, e.getMessage());
        }

        mNotificationSetting.setVibrate(prefs.getBoolean(mUuid + ".vibrate", false));
        mNotificationSetting.setVibratePattern(prefs.getInt(mUuid + ".vibratePattern", 0));
        mNotificationSetting.setVibrateTimes(prefs.getInt(mUuid + ".vibrateTimes", 5));
        mNotificationSetting.setRing(prefs.getBoolean(mUuid + ".ring", true));
        mNotificationSetting.setRingtone(prefs.getString(mUuid + ".ringtone",
                "content://settings/system/notification_sound"));
        mNotificationSetting.setLed(prefs.getBoolean(mUuid + ".led", true));
        mNotificationSetting.setLedColor(prefs.getInt(mUuid + ".ledColor", mChipColor));

        try {
            mFolderDisplayMode = FolderMode.valueOf(prefs.getString(mUuid + ".folderDisplayMode",
                    FolderMode.NOT_SECOND_CLASS.name()));
        } catch (Exception e) {
            mFolderDisplayMode = FolderMode.NOT_SECOND_CLASS;
        }

        try {
            mFolderSyncMode = FolderMode.valueOf(prefs.getString(mUuid + ".folderSyncMode",
                    FolderMode.FIRST_CLASS.name()));
        } catch (Exception e) {
            mFolderSyncMode = FolderMode.FIRST_CLASS;
        }

        try {
            mFolderPushMode = FolderMode.valueOf(prefs.getString(mUuid + ".folderPushMode",
                    FolderMode.FIRST_CLASS.name()));
        } catch (Exception e) {
            mFolderPushMode = FolderMode.FIRST_CLASS;
        }

        try {
            searchableFolders = Searchable.valueOf(prefs.getString(mUuid + ".searchableFolders",
                    Searchable.ALL.name()));
        } catch (Exception e) {
            searchableFolders = Searchable.ALL;
        }

        mIsSignatureBeforeQuotedText =
                prefs.getBoolean(mUuid + ".signatureBeforeQuotedText", false);
        identities = loadIdentities(prefs);

    }


    protected synchronized void delete(Preferences preferences) {
        String[] uuids = preferences.getPreferences().getString("accountUuids", "").split(",");
        StringBuffer sb = new StringBuffer();
        for (int i = 0, length = uuids.length; i < length; i++) {
            if (!uuids[i].equals(mUuid)) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(uuids[i]);
            }
        }
        String accountUuids = sb.toString();
        SharedPreferences.Editor editor = preferences.getPreferences().edit();
        editor.putString("accountUuids", accountUuids);

        editor.remove(mUuid + ".storeUri");
        editor.remove(mUuid + ".localStoreUri");
        editor.remove(mUuid + ".transportUri");
        editor.remove(mUuid + ".description");
        editor.remove(mUuid + ".name");
        editor.remove(mUuid + ".email");
        editor.remove(mUuid + ".alwaysBcc");
        editor.remove(mUuid + ".automaticCheckIntervalMinutes");
        editor.remove(mUuid + ".pushPollOnConnect");
        editor.remove(mUuid + ".saveAllHeaders");
        editor.remove(mUuid + ".idleRefreshMinutes");
        editor.remove(mUuid + ".lastAutomaticCheckTime");
        editor.remove(mUuid + ".latestOldMessageSeenTime");
        editor.remove(mUuid + ".notifyNewMail");
        editor.remove(mUuid + ".notifySelfNewMail");
        editor.remove(mUuid + ".deletePolicy");
        editor.remove(mUuid + ".draftsFolderName");
        editor.remove(mUuid + ".sentFolderName");
        editor.remove(mUuid + ".trashFolderName");
        editor.remove(mUuid + ".archiveFolderName");
        editor.remove(mUuid + ".spamFolderName");
        editor.remove(mUuid + ".autoExpandFolderName");
        editor.remove(mUuid + ".accountNumber");
        editor.remove(mUuid + ".vibrate");
        editor.remove(mUuid + ".vibratePattern");
        editor.remove(mUuid + ".vibrateTimes");
        editor.remove(mUuid + ".ring");
        editor.remove(mUuid + ".ringtone");
        editor.remove(mUuid + ".lastFullSync");
        editor.remove(mUuid + ".folderDisplayMode");
        editor.remove(mUuid + ".folderSyncMode");
        editor.remove(mUuid + ".folderPushMode");
        editor.remove(mUuid + ".folderTargetMode");
        editor.remove(mUuid + ".hideButtonsEnum");
        editor.remove(mUuid + ".signatureBeforeQuotedText");
        editor.remove(mUuid + ".expungePolicy");
        editor.remove(mUuid + ".syncRemoteDeletions");
        editor.remove(mUuid + ".maxPushFolders");
        editor.remove(mUuid + ".searchableFolders");
        editor.remove(mUuid + ".chipColor");
        editor.remove(mUuid + ".led");
        editor.remove(mUuid + ".ledColor");
        editor.remove(mUuid + ".goToUnreadMessageSearch");
        editor.remove(mUuid + ".notificationUnreadCount");
        editor.remove(mUuid + ".subscribedFoldersOnly");
        editor.remove(mUuid + ".maximumPolledMessageAge");
        editor.remove(mUuid + ".maximumAutoDownloadMessageSize");
        editor.remove(mUuid + ".quoteStyle");
        editor.remove(mUuid + ".quotePrefix");
        editor.remove(mUuid + ".showPicturesEnum");
        editor.remove(mUuid + ".replyAfterQuote");
        editor.remove(mUuid + ".cryptoApp");
        editor.remove(mUuid + ".cryptoAutoSignature");
        editor.remove(mUuid + ".enableMoveButtons");
        editor.remove(mUuid + ".hideMoveButtonsEnum");
        for (String type : networkTypes) {
            editor.remove(mUuid + ".useCompression." + type);
        }
        deleteIdentities(preferences.getPreferences(), editor);
        editor.commit();
    }

    public synchronized void save(Preferences preferences) {
        SharedPreferences.Editor editor = preferences.getPreferences().edit();

        if (!preferences.getPreferences().getString("accountUuids", "").contains(mUuid)) {
            Account[] accounts = preferences.getAccounts();
            int[] accountNumbers = new int[accounts.length];
            for (int i = 0; i < accounts.length; i++) {
                accountNumbers[i] = accounts[i].getAccountNumber();
            }
            Arrays.sort(accountNumbers);
            for (int accountNumber : accountNumbers) {
                if (accountNumber > mAccountNumber + 1) {
                    break;
                }
                mAccountNumber = accountNumber;
            }
            mAccountNumber++;

            String accountUuids = preferences.getPreferences().getString("accountUuids", "");
            accountUuids += (accountUuids.length() != 0 ? "," : "") + mUuid;
            editor.putString("accountUuids", accountUuids);
        }

        editor.putString(mUuid + ".storeUri", Utility.base64Encode(mStoreUri));
        editor.putString(mUuid + ".localStorageProvider", mLocalStorageProviderId);
        editor.putString(mUuid + ".transportUri", Utility.base64Encode(mTransportUri));
        editor.putString(mUuid + ".description", mDescription);
        editor.putString(mUuid + ".alwaysBcc", mAlwaysBcc);
        editor.putInt(mUuid + ".automaticCheckIntervalMinutes", mAutomaticCheckIntervalMinutes);
        editor.putInt(mUuid + ".idleRefreshMinutes", mIdleRefreshMinutes);
        editor.putBoolean(mUuid + ".saveAllHeaders", mSaveAllHeaders);
        editor.putBoolean(mUuid + ".pushPollOnConnect", mPushPollOnConnect);
        editor.putInt(mUuid + ".displayCount", mDisplayCount);
        editor.putLong(mUuid + ".lastAutomaticCheckTime", mLastAutomaticCheckTime);
        editor.putLong(mUuid + ".latestOldMessageSeenTime", mLatestOldMessageSeenTime);
        editor.putBoolean(mUuid + ".notifyNewMail", mNotifyNewMail);
        editor.putBoolean(mUuid + ".notifySelfNewMail", mNotifySelfNewMail);
        editor.putBoolean(mUuid + ".notifyMailCheck", mNotifySync);
        editor.putInt(mUuid + ".deletePolicy", mDeletePolicy);
        editor.putString(mUuid + ".inboxFolderName", mInboxFolderName);
        editor.putString(mUuid + ".draftsFolderName", mDraftsFolderName);
        editor.putString(mUuid + ".sentFolderName", mSentFolderName);
        editor.putString(mUuid + ".trashFolderName", mTrashFolderName);
        editor.putString(mUuid + ".archiveFolderName", mArchiveFolderName);
        editor.putString(mUuid + ".spamFolderName", mSpamFolderName);
        editor.putString(mUuid + ".autoExpandFolderName", mAutoExpandFolderName);
        editor.putInt(mUuid + ".accountNumber", mAccountNumber);
        editor.putString(mUuid + ".folderDisplayMode", mFolderDisplayMode.name());
        editor.putString(mUuid + ".folderSyncMode", mFolderSyncMode.name());
        editor.putString(mUuid + ".folderPushMode", mFolderPushMode.name());
        editor.putBoolean(mUuid + ".signatureBeforeQuotedText", this.mIsSignatureBeforeQuotedText);
        editor.putString(mUuid + ".expungePolicy", mExpungePolicy);
        editor.putBoolean(mUuid + ".syncRemoteDeletions", mSyncRemoteDeletions);
        editor.putInt(mUuid + ".maxPushFolders", mMaxPushFolders);
        editor.putString(mUuid + ".searchableFolders", searchableFolders.name());
        editor.putInt(mUuid + ".chipColor", mChipColor);
        editor.putBoolean(mUuid + ".goToUnreadMessageSearch", goToUnreadMessageSearch);
        editor.putBoolean(mUuid + ".notificationUnreadCount", mNotificationShowsUnreadCount);
        editor.putBoolean(mUuid + ".subscribedFoldersOnly", subscribedFoldersOnly);
        editor.putInt(mUuid + ".maximumAutoDownloadMessageSize", maximumAutoDownloadMessageSize);
        editor.putString(mUuid + ".messageFormat", mMessageFormat.name());
        editor.putString(mUuid + ".quoteStyle", mQuoteStyle.name());
        editor.putString(mUuid + ".quotePrefix", mQuotePrefix);
        editor.putBoolean(mUuid + ".defaultQuotedTextShown", mDefaultQuotedTextShown);
        editor.putBoolean(mUuid + ".replyAfterQuote", mReplyAfterQuote);

        editor.putBoolean(mUuid + ".vibrate", mNotificationSetting.shouldVibrate());
        editor.putInt(mUuid + ".vibratePattern", mNotificationSetting.getVibratePattern());
        editor.putInt(mUuid + ".vibrateTimes", mNotificationSetting.getVibrateTimes());
        editor.putBoolean(mUuid + ".ring", mNotificationSetting.shouldRing());
        editor.putString(mUuid + ".ringtone", mNotificationSetting.getRingtone());
        editor.putBoolean(mUuid + ".led", mNotificationSetting.isLed());
        editor.putInt(mUuid + ".ledColor", mNotificationSetting.getLedColor());

        editor.putInt(mUuid + ".attachmentCacheLimitCount", attachmentCacheLimitCount);
        editor.putLong(mUuid + ".slideSleepTimeDuration", slideSleepTimeDuration);
        editor.putLong(mUuid + ".serverSyncTimeDuration", serverSyncTimeDuration);
        editor.putInt(mUuid + ".scaleRatio", scaleRatio);
        editor.putInt(mUuid + ".checkStartId", checkStartId);
        editor.putInt(mUuid + ".checkEndId", checkEndId);
        editor.putBoolean(mUuid + ".isAllSync", isAllSync);
        editor.putBoolean(mUuid + ".isSync", isSync);
        editor.putString(mUuid + ".appRunLatestUid", appRunLatestUid);
        editor.putString(mUuid + ".newMailCheckLatestUid", newMailCheckLatestUid);
        editor.putString(mUuid + ".pastMailCheckLatestUid", pastMailCheckLatestUid);
        editor.putString(mUuid + ".oldAppRunLatestUid", oldAppRunLatestUid);
        editor.putString(mUuid + ".oldNewMailCheckLatestUid", oldNewMailCheckLatestUid);
        editor.putString(mUuid + ".oldPastMailCheckLatestUid", oldPastMailCheckLatestUid);
        editor.putBoolean(mUuid + ".canSleep", canSleep);
        editor.putBoolean(mUuid + ".canDispSlideShowInfo", canDispSlideShowInfo);

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        if (null != appRunLatestDate) {
            editor.putString(sdf.format(appRunLatestDate), null);
        }
        if (null != newMailCheckLatestDate) {
            editor.putString(sdf.format(newMailCheckLatestDate), null);
        }
        if (null != pastMailCheckLatestDate) {
            editor.putString(sdf.format(pastMailCheckLatestDate), null);
        }
        if (null != oldAppRunLatestDate) {
            editor.putString(sdf.format(oldAppRunLatestDate), null);
        }
        if (null != oldNewMailCheckLatestDate) {
            editor.putString(sdf.format(oldNewMailCheckLatestDate), null);
        }
        if (null != oldPastMailCheckLatestDate) {
            editor.putString(sdf.format(oldPastMailCheckLatestDate), null);
        }

        for (String type : networkTypes) {
            Boolean useCompression = compressionMap.get(type);
            if (useCompression != null) {
                editor.putBoolean(mUuid + ".useCompression." + type, useCompression);
            }
        }
        saveIdentities(preferences.getPreferences(), editor);

        editor.commit();

    }

    public void resetVisibleLimits() {
        try {
            getLocalStore().resetVisibleLimits(getDisplayCount());
        } catch (MessagingException e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Unable to reset visible limits", e);
        }

    }

    /**
     * @param context
     * @return <code>null</code> if not available
     * @throws MessagingException
     * @see {@link #isAvailable(Context)}
     */
    public AccountStats getStats(Context context) throws MessagingException {
        if (!isAvailable(context)) {
            return null;
        }
        long startTime = System.currentTimeMillis();
        AccountStats stats = new AccountStats();
        LocalStore localStore = getLocalStore();
        if (RakuPhotoMail.measureAccounts()) {
            stats.size = localStore.getSize();
        }
        localStore.getMessageCounts(stats);
        long endTime = System.currentTimeMillis();
        if (RakuPhotoMail.DEBUG)
            Log.d(RakuPhotoMail.LOG_TAG,
                    "Account.getStats() on " + getDescription() + " took " + (endTime - startTime) +
                            " ms;");
        return stats;
    }


    public synchronized void setChipColor(int color) {
        mChipColor = color;
    }

    public synchronized int getChipColor() {
        return mChipColor;
    }


    public ColorChip generateColorChip() {
        return new ColorChip(mChipColor);
    }


    public String getUuid() {
        return mUuid;
    }

    public Uri getContentUri() {
        return Uri.parse("content://accounts/" + getUuid());
    }

    public synchronized String getStoreUri() {
        return mStoreUri;
    }

    public synchronized void setStoreUri(String storeUri) {
        this.mStoreUri = storeUri;
    }

    public synchronized String getTransportUri() {
        return mTransportUri;
    }

    public synchronized void setTransportUri(String transportUri) {
        this.mTransportUri = transportUri;
    }

    public synchronized String getDescription() {
        return mDescription;
    }

    public synchronized void setDescription(String description) {
        this.mDescription = description;
    }

    public synchronized String getName() {
        return identities.get(0).getName();
    }

    public synchronized void setName(String name) {
        identities.get(0).setName(name);
    }

    public synchronized boolean getSignatureUse() {
        return identities.get(0).getSignatureUse();
    }

    public synchronized void setSignatureUse(boolean signatureUse) {
        identities.get(0).setSignatureUse(signatureUse);
    }

    public synchronized String getSignature() {
        return identities.get(0).getSignature();
    }

    public synchronized void setSignature(String signature) {
        identities.get(0).setSignature(signature);
    }

    public synchronized String getEmail() {
        return identities.get(0).getEmail();
    }

    public synchronized void setEmail(String email) {
        identities.get(0).setEmail(email);
    }

    public synchronized String getAlwaysBcc() {
        return mAlwaysBcc;
    }

    public synchronized void setAlwaysBcc(String alwaysBcc) {
        this.mAlwaysBcc = alwaysBcc;
    }

    /* Have we sent a new mail notification on this account */
    public boolean isRingNotified() {
        return mRingNotified;
    }

    public void setRingNotified(boolean ringNotified) {
        mRingNotified = ringNotified;
    }

    public String getLocalStorageProviderId() {
        return mLocalStorageProviderId;
    }

    public void setLocalStorageProviderId(String id) {

        if (!mLocalStorageProviderId.equals(id)) {

            boolean successful = false;
            try {
                switchLocalStorage(id);
                successful = true;
            } catch (MessagingException e) {
                Log.e(RakuPhotoMail.LOG_TAG, "Switching local storage provider from " +
                        mLocalStorageProviderId + " to " + id + " failed.", e);
            } finally {
                // if migration to/from SD-card failed once, it will fail again.
                if (!successful) {
                    return;
                }
            }

            mLocalStorageProviderId = id;
        }

    }

    public synchronized int getAutomaticCheckIntervalMinutes() {
        return mAutomaticCheckIntervalMinutes;
    }

    /**
     * @param automaticCheckIntervalMinutes or -1 for never.
     */
    public synchronized boolean setAutomaticCheckIntervalMinutes(
            int automaticCheckIntervalMinutes) {
        int oldInterval = this.mAutomaticCheckIntervalMinutes;
        this.mAutomaticCheckIntervalMinutes = automaticCheckIntervalMinutes;

        return (oldInterval != automaticCheckIntervalMinutes);
    }

    public synchronized int getDisplayCount() {
        return mDisplayCount;
    }

    public synchronized void setDisplayCount(int displayCount) {
        if (displayCount != -1) {
            this.mDisplayCount = displayCount;
        } else {
            this.mDisplayCount = RakuPhotoMail.DEFAULT_VISIBLE_LIMIT;
        }
        resetVisibleLimits();
    }

    public synchronized long getLastAutomaticCheckTime() {
        return mLastAutomaticCheckTime;
    }

    public synchronized void setLastAutomaticCheckTime(long lastAutomaticCheckTime) {
        this.mLastAutomaticCheckTime = lastAutomaticCheckTime;
    }

    public synchronized long getLatestOldMessageSeenTime() {
        return mLatestOldMessageSeenTime;
    }

    public synchronized void setLatestOldMessageSeenTime(long latestOldMessageSeenTime) {
        this.mLatestOldMessageSeenTime = latestOldMessageSeenTime;
    }

    public synchronized boolean isNotifyNewMail() {
        return mNotifyNewMail;
    }

    public synchronized void setNotifyNewMail(boolean notifyNewMail) {
        this.mNotifyNewMail = notifyNewMail;
    }

    public synchronized int getDeletePolicy() {
        return mDeletePolicy;
    }

    public synchronized void setDeletePolicy(int deletePolicy) {
        this.mDeletePolicy = deletePolicy;
    }


    public boolean isSpecialFolder(String folderName) {
        if (folderName != null && (folderName.equalsIgnoreCase(getInboxFolderName()) ||
                folderName.equals(getTrashFolderName()) ||
                folderName.equals(getDraftsFolderName()) ||
                folderName.equals(getArchiveFolderName()) ||
                folderName.equals(getSpamFolderName()) ||
                folderName.equals(getOutboxFolderName()) ||
                folderName.equals(getSentFolderName()) ||
                folderName.equals(getErrorFolderName()))) {
            return true;

        } else {
            return false;
        }

    }


    public synchronized String getDraftsFolderName() {
        return mDraftsFolderName;
    }

    public synchronized void setDraftsFolderName(String draftsFolderName) {
        mDraftsFolderName = draftsFolderName;
    }

    public synchronized String getSentFolderName() {
        return mSentFolderName;
    }

    public synchronized String getErrorFolderName() {
        return RakuPhotoMail.ERROR_FOLDER_NAME;
    }

    public synchronized void setSentFolderName(String sentFolderName) {
        mSentFolderName = sentFolderName;
    }

    public synchronized String getTrashFolderName() {
        return mTrashFolderName;
    }

    public synchronized void setTrashFolderName(String trashFolderName) {
        mTrashFolderName = trashFolderName;
    }

    public synchronized String getArchiveFolderName() {
        return mArchiveFolderName;
    }

    public synchronized void setArchiveFolderName(String archiveFolderName) {
        mArchiveFolderName = archiveFolderName;
    }

    public synchronized String getSpamFolderName() {
        return mSpamFolderName;
    }

    public synchronized void setSpamFolderName(String spamFolderName) {
        mSpamFolderName = spamFolderName;
    }

    public synchronized String getOutboxFolderName() {
        return OUTBOX;
    }

    public synchronized String getAutoExpandFolderName() {
        return mAutoExpandFolderName;
    }

    public synchronized void setAutoExpandFolderName(String autoExpandFolderName) {
        mAutoExpandFolderName = autoExpandFolderName;
    }

    public synchronized int getAccountNumber() {
        return mAccountNumber;
    }

    public synchronized FolderMode getFolderDisplayMode() {
        return mFolderDisplayMode;
    }

    public synchronized boolean setFolderDisplayMode(FolderMode displayMode) {
        FolderMode oldDisplayMode = mFolderDisplayMode;
        mFolderDisplayMode = displayMode;
        return oldDisplayMode != displayMode;
    }

    public synchronized FolderMode getFolderSyncMode() {
        return mFolderSyncMode;
    }

    public synchronized boolean setFolderSyncMode(FolderMode syncMode) {
        FolderMode oldSyncMode = mFolderSyncMode;
        mFolderSyncMode = syncMode;

        if (syncMode == FolderMode.NONE && oldSyncMode != FolderMode.NONE) {
            return true;
        }
        if (syncMode != FolderMode.NONE && oldSyncMode == FolderMode.NONE) {
            return true;
        }
        return false;
    }

    public synchronized FolderMode getFolderPushMode() {
        return mFolderPushMode;
    }

    public synchronized boolean setFolderPushMode(FolderMode pushMode) {
        FolderMode oldPushMode = mFolderPushMode;

        mFolderPushMode = pushMode;
        return pushMode != oldPushMode;
    }

    public synchronized boolean isShowOngoing() {
        return mNotifySync;
    }

    public synchronized void setShowOngoing(boolean showOngoing) {
        this.mNotifySync = showOngoing;
    }

    public synchronized boolean isSignatureBeforeQuotedText() {
        return mIsSignatureBeforeQuotedText;
    }

    public synchronized void setSignatureBeforeQuotedText(boolean mIsSignatureBeforeQuotedText) {
        this.mIsSignatureBeforeQuotedText = mIsSignatureBeforeQuotedText;
    }

    public synchronized boolean isNotifySelfNewMail() {
        return mNotifySelfNewMail;
    }

    public synchronized void setNotifySelfNewMail(boolean notifySelfNewMail) {
        mNotifySelfNewMail = notifySelfNewMail;
    }

    public synchronized String getExpungePolicy() {
        return mExpungePolicy;
    }

    public synchronized void setExpungePolicy(String expungePolicy) {
        mExpungePolicy = expungePolicy;
    }

    public synchronized int getMaxPushFolders() {
        return mMaxPushFolders;
    }

    public synchronized boolean setMaxPushFolders(int maxPushFolders) {
        int oldMaxPushFolders = mMaxPushFolders;
        mMaxPushFolders = maxPushFolders;
        return oldMaxPushFolders != maxPushFolders;
    }

    public LocalStore getLocalStore() throws MessagingException {
        return Store.getLocalInstance(this, RakuPhotoMail.app);
    }

    public Store getRemoteStore() throws MessagingException {
        return Store.getRemoteInstance(this);
    }

    @Override
    public synchronized String toString() {
        return mDescription;
    }

    public synchronized void setCompression(String networkType, boolean useCompression) {
        compressionMap.put(networkType, useCompression);
    }

    public synchronized boolean useCompression(String networkType) {
        Boolean useCompression = compressionMap.get(networkType);
        if (useCompression == null) {
            return true;
        } else {
            return useCompression;
        }
    }

    public boolean useCompression(int type) {
        String networkType = TYPE_OTHER;
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
                networkType = TYPE_MOBILE;
                break;
            case ConnectivityManager.TYPE_WIFI:
                networkType = TYPE_WIFI;
                break;
        }
        return useCompression(networkType);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Account) {
            return ((Account) o).mUuid.equals(mUuid);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return mUuid.hashCode();
    }


    private synchronized List<Identity> loadIdentities(SharedPreferences prefs) {
        List<Identity> newIdentities = new ArrayList<Identity>();
        int ident = 0;
        boolean gotOne = false;
        do {
            gotOne = false;
            String name = prefs.getString(mUuid + ".name." + ident, null);
            String email = prefs.getString(mUuid + ".email." + ident, null);
            boolean signatureUse = prefs.getBoolean(mUuid + ".signatureUse." + ident, true);
            String signature = prefs.getString(mUuid + ".signature." + ident, null);
            String description = prefs.getString(mUuid + ".description." + ident, null);
            final String replyTo = prefs.getString(mUuid + ".replyTo." + ident, null);
            if (email != null) {
                Identity identity = new Identity();
                identity.setName(name);
                identity.setEmail(email);
                identity.setSignatureUse(signatureUse);
                identity.setSignature(signature);
                identity.setDescription(description);
                identity.setReplyTo(replyTo);
                newIdentities.add(identity);
                gotOne = true;
            }
            ident++;
        } while (gotOne);

        if (newIdentities.size() == 0) {
            String name = prefs.getString(mUuid + ".name", null);
            String email = prefs.getString(mUuid + ".email", null);
            boolean signatureUse = prefs.getBoolean(mUuid + ".signatureUse", true);
            String signature = prefs.getString(mUuid + ".signature", null);
            Identity identity = new Identity();
            identity.setName(name);
            identity.setEmail(email);
            identity.setSignatureUse(signatureUse);
            identity.setSignature(signature);
            identity.setDescription(email);
            newIdentities.add(identity);
        }

        return newIdentities;
    }

    private synchronized void deleteIdentities(SharedPreferences prefs,
                                               SharedPreferences.Editor editor) {
        int ident = 0;
        boolean gotOne = false;
        do {
            gotOne = false;
            String email = prefs.getString(mUuid + ".email." + ident, null);
            if (email != null) {
                editor.remove(mUuid + ".name." + ident);
                editor.remove(mUuid + ".email." + ident);
                editor.remove(mUuid + ".signatureUse." + ident);
                editor.remove(mUuid + ".signature." + ident);
                editor.remove(mUuid + ".description." + ident);
                editor.remove(mUuid + ".replyTo." + ident);
                gotOne = true;
            }
            ident++;
        } while (gotOne);
    }

    private synchronized void saveIdentities(SharedPreferences prefs,
                                             SharedPreferences.Editor editor) {
        deleteIdentities(prefs, editor);
        int ident = 0;

        for (Identity identity : identities) {
            editor.putString(mUuid + ".name." + ident, identity.getName());
            editor.putString(mUuid + ".email." + ident, identity.getEmail());
            editor.putBoolean(mUuid + ".signatureUse." + ident, identity.getSignatureUse());
            editor.putString(mUuid + ".signature." + ident, identity.getSignature());
            editor.putString(mUuid + ".description." + ident, identity.getDescription());
            editor.putString(mUuid + ".replyTo." + ident, identity.getReplyTo());
            ident++;
        }
    }

    public synchronized List<Identity> getIdentities() {
        return identities;
    }

    public synchronized void setIdentities(List<Identity> newIdentities) {
        identities = new ArrayList<Identity>(newIdentities);
    }

    public synchronized Identity getIdentity(int i) {
        if (i < identities.size()) {
            return identities.get(i);
        }
        return null;
    }

    public boolean isAnIdentity(Address[] addrs) {
        if (addrs == null) {
            return false;
        }
        for (Address addr : addrs) {
            if (findIdentity(addr) != null) {
                return true;
            }
        }

        return false;
    }

    public boolean isAnIdentity(Address addr) {
        return findIdentity(addr) != null;
    }

    public synchronized Identity findIdentity(Address addr) {
        for (Identity identity : identities) {
            String email = identity.getEmail();
            if (email != null && email.equalsIgnoreCase(addr.getAddress())) {
                return identity;
            }
        }
        return null;
    }

    public synchronized Searchable getSearchableFolders() {
        return searchableFolders;
    }

    public synchronized void setSearchableFolders(Searchable searchableFolders) {
        this.searchableFolders = searchableFolders;
    }

    public synchronized int getIdleRefreshMinutes() {
        return mIdleRefreshMinutes;
    }

    public synchronized void setIdleRefreshMinutes(int idleRefreshMinutes) {
        mIdleRefreshMinutes = idleRefreshMinutes;
    }

    public synchronized boolean isPushPollOnConnect() {
        return mPushPollOnConnect;
    }

    public synchronized void setPushPollOnConnect(boolean pushPollOnConnect) {
        mPushPollOnConnect = pushPollOnConnect;
    }

    public synchronized boolean saveAllHeaders() {
        return mSaveAllHeaders;
    }

    public synchronized void setSaveAllHeaders(boolean saveAllHeaders) {
        mSaveAllHeaders = saveAllHeaders;
    }

    /**
     * Are we storing out localStore on the SD-card instead of the local device
     * memory?<br/>
     * Only to be called durin initial account-setup!<br/>
     * Side-effect: changes {@link #mLocalStorageProviderId}.
     *
     * @param newStorageProviderId Never <code>null</code>.
     * @throws MessagingException
     */
    public void switchLocalStorage(final String newStorageProviderId) throws MessagingException {
        if (!mLocalStorageProviderId.equals(newStorageProviderId)) {
            getLocalStore().switchLocalStorage(newStorageProviderId);
        }
    }

    public synchronized boolean goToUnreadMessageSearch() {
        return goToUnreadMessageSearch;
    }

    public synchronized void setGoToUnreadMessageSearch(boolean goToUnreadMessageSearch) {
        this.goToUnreadMessageSearch = goToUnreadMessageSearch;
    }

    public boolean isNotificationShowsUnreadCount() {
        return mNotificationShowsUnreadCount;
    }

    public void setNotificationShowsUnreadCount(boolean notificationShowsUnreadCount) {
        this.mNotificationShowsUnreadCount = notificationShowsUnreadCount;
    }

    public synchronized boolean subscribedFoldersOnly() {
        return subscribedFoldersOnly;
    }

    public synchronized void setSubscribedFoldersOnly(boolean subscribedFoldersOnly) {
        this.subscribedFoldersOnly = subscribedFoldersOnly;
    }

    public synchronized int getMaximumAutoDownloadMessageSize() {
        return maximumAutoDownloadMessageSize;
    }

    public synchronized void setMaximumAutoDownloadMessageSize(int maximumAutoDownloadMessageSize) {
        this.maximumAutoDownloadMessageSize = maximumAutoDownloadMessageSize;
    }

    public Date getEarliestPollDate() {
        int age = -1;
        if (age >= 0) {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);
            if (age < 28) {
                now.add(Calendar.DATE, age * -1);
            } else switch (age) {
                case 28:
                    now.add(Calendar.MONTH, -1);
                    break;
                case 56:
                    now.add(Calendar.MONTH, -2);
                    break;
                case 84:
                    now.add(Calendar.MONTH, -3);
                    break;
                case 168:
                    now.add(Calendar.MONTH, -6);
                    break;
                case 365:
                    now.add(Calendar.YEAR, -1);
                    break;
            }

            return now.getTime();
        } else {
            return null;
        }
    }

    public void setMessageFormat(MessageFormat messageFormat) {
        this.mMessageFormat = messageFormat;
    }

    public void setQuoteStyle(QuoteStyle quoteStyle) {
        this.mQuoteStyle = quoteStyle;
    }

    public synchronized void setQuotePrefix(String quotePrefix) {
        mQuotePrefix = quotePrefix;
    }

    public synchronized void setDefaultQuotedTextShown(boolean shown) {
        mDefaultQuotedTextShown = shown;
    }

    public synchronized void setReplyAfterQuote(boolean replyAfterQuote) {
        mReplyAfterQuote = replyAfterQuote;
    }

    public String getInboxFolderName() {
        return mInboxFolderName;
    }

    public void setInboxFolderName(String mInboxFolderName) {
        this.mInboxFolderName = mInboxFolderName;
    }

    public synchronized boolean syncRemoteDeletions() {
        return mSyncRemoteDeletions;
    }

    public synchronized void setSyncRemoteDeletions(boolean syncRemoteDeletions) {
        mSyncRemoteDeletions = syncRemoteDeletions;
    }

    public synchronized NotificationSetting getNotificationSetting() {
        return mNotificationSetting;
    }

    public boolean isAvailable(Context context) {
        String localStorageProviderId = getLocalStorageProviderId();
        if (localStorageProviderId == null) {
            return true; // defaults to internal memory
        }
        return StorageManager.getInstance(RakuPhotoMail.app).isReady(localStorageProviderId);
    }

    public int getMessageLimitCountFromDb() {
        return messageLimitCountFromDb;
    }

    public int getMessageLimitCountFromRemote() {
        return messageLimitCountFromRemote;
    }

    public int getAttachmentCacheLimitCount() {
        return attachmentCacheLimitCount;
    }

    public long getSlideSleepTime() {
        return slideSleepTimeDuration;
    }

    public void setSlideSleepTime(long sleepTime) {
        this.slideSleepTimeDuration = sleepTime;
    }

    public long getServerSyncTimeDuration() {
        return serverSyncTimeDuration;
    }

    public void setServerSyncTimeDuration(long serverSyncTimeDuration) {
        this.serverSyncTimeDuration = serverSyncTimeDuration;
    }

    public int getScaleRatio() {
        return this.scaleRatio;
    }

    public void setScaleRatio(int scaleRatio) {
        this.scaleRatio = scaleRatio;
    }

    public int getCheckStartId() {
        return this.checkStartId;
    }

    public void setCheckStartId(int checkStartId) {
        this.checkStartId = checkStartId;
    }

    public int getCheckEndId() {
        return this.checkEndId;
    }

    public void setCheckEndId(int checkEndId) {
        this.checkEndId = checkEndId;
    }

    public boolean isAllSync() {
        return this.isAllSync;
    }

    public void setAllSync(boolean tf) {
        this.isAllSync = tf;
    }


    public boolean isSync() {
        return this.isSync;
    }

    public void setSync(boolean tf) {
        this.isSync = tf;
    }

    public String getAppRunLatestUid() {
        return this.appRunLatestUid;
    }

    public void setAppRunLatestUid(String appRunLatestUid) {
        this.oldAppRunLatestUid = this.appRunLatestUid;
        this.appRunLatestUid = appRunLatestUid;
    }

    public void setAppRunLatestDate(Date appRunLatestDate) {
        this.oldAppRunLatestDate = this.appRunLatestDate;
        this.appRunLatestDate = appRunLatestDate;
    }

    public String getNewMailCheckLatestUid() {
        return this.newMailCheckLatestUid;
    }

    public void setNewMailCheckLatestUid(String newMailCheckLatestUid) {
        this.oldNewMailCheckLatestUid = this.newMailCheckLatestUid;
        this.newMailCheckLatestUid = newMailCheckLatestUid;
    }

    public Date getNewMailCheckLatestDate() {
        return this.newMailCheckLatestDate;
    }

    public void setNewMailCheckLatestDate(Date newMailCheckLatestDate) {
        this.oldNewMailCheckLatestDate = this.newMailCheckLatestDate;
        this.newMailCheckLatestDate = newMailCheckLatestDate;
    }

    public String getPastMailCheckLatestUid() {
        return this.pastMailCheckLatestUid;
    }

    public void setPastMailCheckLatestUid(String pastMailCheckLatestUid) {
        this.oldPastMailCheckLatestUid = this.pastMailCheckLatestUid;
        this.pastMailCheckLatestUid = pastMailCheckLatestUid;
    }

    public void setPastMailCheckLatestDate(Date pastMailCheckLatestDate) {
        this.oldPastMailCheckLatestDate = this.pastMailCheckLatestDate;
        this.pastMailCheckLatestDate = pastMailCheckLatestDate;
    }

    public boolean canSleep() {
        return this.canSleep;
    }

    public void setCanSleep(boolean canSleep) {
        this.canSleep = canSleep;
    }

    public boolean canDispSlideShowInfo() {
        return this.canDispSlideShowInfo;
    }

    public void setCanDispSlideShowInfo(boolean canDispSlideShowInfo) {
        this.canDispSlideShowInfo = canDispSlideShowInfo;
    }



}
