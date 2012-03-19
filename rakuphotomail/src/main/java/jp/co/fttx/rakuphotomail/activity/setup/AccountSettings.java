package jp.co.fttx.rakuphotomail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.view.KeyEvent;
import jp.co.fttx.rakuphotomail.Account;
import jp.co.fttx.rakuphotomail.Account.QuoteStyle;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.*;
import jp.co.fttx.rakuphotomail.mail.Folder;
import jp.co.fttx.rakuphotomail.mail.Store;
import jp.co.fttx.rakuphotomail.mail.store.LocalStore.LocalFolder;
import jp.co.fttx.rakuphotomail.mail.store.StorageManager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class AccountSettings extends RakuphotoPreferenceActivity {
    private static final String EXTRA_ACCOUNT = "account";

    private static final String PREFERENCE_DESCRIPTION = "account_description";
    private static final String PREFERENCE_INCOMING = "incoming";
    private static final String PREFERENCE_OUTGOING = "outgoing";
    private static final String PREFERENCE_AUTO_EXPAND_FOLDER = "account_setup_auto_expand_folder";
    private static final String PREFERENCE_MESSAGE_SIZE = "account_download_size";
    private static final String PREFERENCE_SLIDE_CHANGE_DURATION = "account_slide_change_duration";
    private static final String PREFERENCE_SCALE_RATIO = "account_scale_ratio";
    private static final String PREFERENCE_SERVER_SYNC = "account_server_sync";
    private static final String PREFERENCE_DOWNLOAD_CACHE = "account_download_cache";

    private static final String PREFERENCE_LOCAL_STORAGE_PROVIDER = "local_storage_provider";
    private static final String PREFERENCE_DRAFTS_FOLDER = "drafts_folder";
    private static final String PREFERENCE_SENT_FOLDER = "sent_folder";
    private static final String PREFERENCE_SPAM_FOLDER = "spam_folder";
    private static final String PREFERENCE_TRASH_FOLDER = "trash_folder";


    private Account mAccount;
    private boolean mIsPushCapable = false;

    private EditTextPreference mAccountDescription;
    private ListPreference mSlideChangeDuration;
    private ListPreference mScaleRatio;
    private ListPreference mServerSync;
    private ListPreference mDownloadCache;
    private ListPreference mMessageSize;
    private ListPreference mAutoExpandFolder;

    private ListPreference mLocalStorageProvider;

    private ListPreference mDraftsFolder;
    private ListPreference mSentFolder;
    private ListPreference mSpamFolder;
    private ListPreference mTrashFolder;


    public static void actionSettings(Context context, Account account) {
        Intent i = new Intent(context, AccountSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        try {
            final Store store = mAccount.getRemoteStore();
            mIsPushCapable = store.isPushCapable();
        } catch (Exception e) {
            Log.e(RakuPhotoMail.LOG_TAG, "Could not get remote store", e);
        }

        addPreferencesFromResource(R.xml.account_settings_preferences);

        mAccountDescription = (EditTextPreference) findPreference(PREFERENCE_DESCRIPTION);
        mAccountDescription.setSummary(mAccount.getDescription());
        mAccountDescription.setText(mAccount.getDescription());
        mAccountDescription.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String summary = newValue.toString();
                mAccountDescription.setSummary(summary);
                mAccountDescription.setText(summary);
                return false;
            }
        });

        mSlideChangeDuration = (ListPreference) findPreference(PREFERENCE_SLIDE_CHANGE_DURATION);
        mSlideChangeDuration.setValue(String.valueOf(mAccount.getSlideSleepTime()));
        mSlideChangeDuration.setSummary(mSlideChangeDuration.getEntry());
        mSlideChangeDuration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String summary = newValue.toString();
                int index = mSlideChangeDuration.findIndexOfValue(summary);
                mSlideChangeDuration.setSummary(mSlideChangeDuration.getEntries()[index]);
                mSlideChangeDuration.setValue(summary);
                return false;
            }
        });

        mScaleRatio = (ListPreference) findPreference(PREFERENCE_SCALE_RATIO);
        mScaleRatio.setValue(String.valueOf(mAccount.getScaleRatio()));
        mScaleRatio.setSummary(mScaleRatio.getEntry());
        mScaleRatio.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String summary = newValue.toString();
                int index = mScaleRatio.findIndexOfValue(summary);
                mScaleRatio.setSummary(mScaleRatio.getEntries()[index]);
                mScaleRatio.setValue(summary);
                return false;
            }
        });

        mServerSync = (ListPreference) findPreference(PREFERENCE_SERVER_SYNC);
        mServerSync.setValue(String.valueOf(mAccount.getServerSyncTimeDuration()));
        mServerSync.setSummary(mServerSync.getEntry());
        mServerSync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String summary = newValue.toString();
                int index = mServerSync.findIndexOfValue(summary);
                mServerSync.setSummary(mServerSync.getEntries()[index]);
                mServerSync.setValue(summary);
                return false;
            }
        });

//        mDownloadCache = (ListPreference) findPreference(PREFERENCE_DOWNLOAD_CACHE);
//        mDownloadCache.setValue(String.valueOf(mAccount.getAttachmentCacheLimitCount()));
//        mDownloadCache.setSummary(mDownloadCache.getEntry());
//        mDownloadCache.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                final String summary = newValue.toString();
//                int index = mDownloadCache.findIndexOfValue(summary);
//                mDownloadCache.setSummary(mDownloadCache.getEntries()[index]);
//                mDownloadCache.setValue(summary);
//                return false;
//            }
//        });

        mMessageSize = (ListPreference) findPreference(PREFERENCE_MESSAGE_SIZE);
        mMessageSize.setValue(String.valueOf(mAccount.getMaximumAutoDownloadMessageSize()));
        mMessageSize.setSummary(mMessageSize.getEntry());
        mMessageSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String summary = newValue.toString();
                int index = mMessageSize.findIndexOfValue(summary);
                mMessageSize.setSummary(mMessageSize.getEntries()[index]);
                mMessageSize.setValue(summary);
                return false;
            }
        });

        mLocalStorageProvider = (ListPreference) findPreference(PREFERENCE_LOCAL_STORAGE_PROVIDER);
        {
            final Map<String, String> providers;
            providers = StorageManager.getInstance(RakuPhotoMail.app).getAvailableProviders();
            int i = 0;
            final String[] providerLabels = new String[providers.size()];
            final String[] providerIds = new String[providers.size()];
            for (final Map.Entry<String, String> entry : providers.entrySet()) {
                providerIds[i] = entry.getKey();
                providerLabels[i] = entry.getValue();
                i++;
            }
            mLocalStorageProvider.setEntryValues(providerIds);
            mLocalStorageProvider.setEntries(providerLabels);
            mLocalStorageProvider.setValue(mAccount.getLocalStorageProviderId());
            mLocalStorageProvider.setSummary(providers.get(mAccount.getLocalStorageProviderId()));

            mLocalStorageProvider.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mLocalStorageProvider.setSummary(providers.get(newValue));
                    return true;
                }
            });
        }

        new PopulateFolderPrefsTask().execute();

        findPreference(PREFERENCE_INCOMING).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        onIncomingSettings();
                        return true;
                    }
                });

        findPreference(PREFERENCE_OUTGOING).setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        onOutgoingSettings();
                        return true;
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void saveSettings() {

        mAccount.setDescription(mAccountDescription.getText());
        mAccount.setNotifyNewMail(false);
        mAccount.setNotifySelfNewMail(false);
        mAccount.setShowOngoing(false);
        mAccount.setDisplayCount(0);
        mAccount.setMaximumAutoDownloadMessageSize(Integer.parseInt(mMessageSize.getValue()));
        mAccount.setSlideSleepTime(Long.parseLong(mSlideChangeDuration.getValue()));
        mAccount.setScaleRatio(Integer.parseInt(mScaleRatio.getValue()));
        mAccount.setServerSyncTimeDuration(Long.parseLong(mServerSync.getValue()));
        mAccount.setAttachmentCacheLimitCount(Integer.parseInt(mDownloadCache.getValue()));
        mAccount.getNotificationSetting().setVibrate(false);
        mAccount.getNotificationSetting().setVibratePattern(0);
        mAccount.getNotificationSetting().setVibrateTimes(0);
        mAccount.getNotificationSetting().setLed(false);
        mAccount.setGoToUnreadMessageSearch(false);
        mAccount.setNotificationShowsUnreadCount(true);
        mAccount.setDeletePolicy(0);
        mAccount.setExpungePolicy("EXPUNGE_IMMEDIATELY");
        mAccount.setSyncRemoteDeletions(true);
        mAccount.setSaveAllHeaders(true);
        mAccount.setMessageFormat(Account.MessageFormat.valueOf("HTML"));
        mAccount.setQuoteStyle(QuoteStyle.valueOf("PREFIX"));
        mAccount.setQuotePrefix(">");
        mAccount.setDefaultQuotedTextShown(false);
        mAccount.setReplyAfterQuote(false);
        mAccount.setLocalStorageProviderId(mLocalStorageProvider.getValue());

        mAccount.setAutoExpandFolderName(reverseTranslateFolder(mAutoExpandFolder.getValue()));

        mAccount.setDraftsFolderName(mDraftsFolder.getValue());
        mAccount.setSentFolderName(mSentFolder.getValue());
        mAccount.setSpamFolderName(mSpamFolder.getValue());
        mAccount.setTrashFolderName(mTrashFolder.getValue());


        if (mIsPushCapable) {
            mAccount.setPushPollOnConnect(false);
            mAccount.setIdleRefreshMinutes(1);
            mAccount.setMaxPushFolders(10);
        }

        mAccount.save(Preferences.getPreferences(this));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveSettings();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onIncomingSettings() {
        AccountSetupIncoming.actionEditIncomingSettings(this, mAccount);
    }

    private void onOutgoingSettings() {
        AccountSetupOutgoing.actionEditOutgoingSettings(this, mAccount);
    }

    private String translateFolder(String in) {
        if (mAccount.getInboxFolderName().equalsIgnoreCase(in)) {
            return getString(R.string.special_mailbox_name_inbox);
        } else {
            return in;
        }
    }

    private String reverseTranslateFolder(String in) {
        if (getString(R.string.special_mailbox_name_inbox).equals(in)) {
            return mAccount.getInboxFolderName();
        } else {
            return in;
        }
    }

    private class PopulateFolderPrefsTask extends AsyncTask<Void, Void, Void> {
        List<? extends Folder> folders = new LinkedList<LocalFolder>();
        String[] allFolderValues;
        String[] allFolderLabels;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                folders = mAccount.getLocalStore().getPersonalNamespaces(false);
            } catch (Exception e) {
            }

            Iterator<? extends Folder> iter = folders.iterator();
            while (iter.hasNext()) {
                Folder folder = iter.next();
                if (mAccount.getOutboxFolderName().equals(folder.getName())) {
                    iter.remove();
                }
            }

            allFolderValues = new String[folders.size() + 1];
            allFolderLabels = new String[folders.size() + 1];

            allFolderValues[0] = RakuPhotoMail.FOLDER_NONE;
            allFolderLabels[0] = RakuPhotoMail.FOLDER_NONE;

            int i = 1;
            for (Folder folder : folders) {
                allFolderLabels[i] = folder.getName();
                allFolderValues[i] = folder.getName();
                i++;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mAutoExpandFolder = (ListPreference) findPreference(PREFERENCE_AUTO_EXPAND_FOLDER);
            mAutoExpandFolder.setEnabled(false);
            mDraftsFolder = (ListPreference) findPreference(PREFERENCE_DRAFTS_FOLDER);
            mDraftsFolder.setEnabled(false);
            mSentFolder = (ListPreference) findPreference(PREFERENCE_SENT_FOLDER);
            mSentFolder.setEnabled(false);
            mSpamFolder = (ListPreference) findPreference(PREFERENCE_SPAM_FOLDER);
            mSpamFolder.setEnabled(false);
            mTrashFolder = (ListPreference) findPreference(PREFERENCE_TRASH_FOLDER);
            mTrashFolder.setEnabled(false);

        }

        @Override
        protected void onPostExecute(Void res) {
            initListPreference(mAutoExpandFolder, mAccount.getAutoExpandFolderName(), allFolderLabels, allFolderValues);
            initListPreference(mDraftsFolder, mAccount.getDraftsFolderName(), allFolderLabels, allFolderValues);
            initListPreference(mSentFolder, mAccount.getSentFolderName(), allFolderLabels, allFolderValues);
            initListPreference(mSpamFolder, mAccount.getSpamFolderName(), allFolderLabels, allFolderValues);
            initListPreference(mTrashFolder, mAccount.getTrashFolderName(), allFolderLabels, allFolderValues);
            mAutoExpandFolder.setEnabled(true);
            mDraftsFolder.setEnabled(true);
            mSentFolder.setEnabled(true);
            mSpamFolder.setEnabled(true);
            mTrashFolder.setEnabled(true);
        }
    }
}
