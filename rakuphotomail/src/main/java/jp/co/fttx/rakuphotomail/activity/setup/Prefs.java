package jp.co.fttx.rakuphotomail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;
import android.widget.Toast;
import jp.co.fttx.rakuphotomail.Preferences;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.ColorPickerDialog;
import jp.co.fttx.rakuphotomail.activity.RakuphotoPreferenceActivity;
import jp.co.fttx.rakuphotomail.helper.DateFormatter;
import jp.co.fttx.rakuphotomail.helper.FileBrowserHelper;
import jp.co.fttx.rakuphotomail.helper.FileBrowserHelper.FileBrowserFailOverCallback;
import jp.co.fttx.rakuphotomail.preferences.CheckBoxListPreference;
import jp.co.fttx.rakuphotomail.preferences.TimePickerPreference;
import jp.co.fttx.rakuphotomail.service.MailService;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;


public class Prefs extends RakuphotoPreferenceActivity {

    /**
     * Immutable empty {@link CharSequence} array
     */
    private static final CharSequence[] EMPTY_CHAR_SEQUENCE_ARRAY = new CharSequence[0];

    /*
     * Keys of the preferences defined in res/xml/global_preferences.xml
     */
    private static final String PREFERENCE_LANGUAGE = "language";
    private static final String PREFERENCE_THEME = "theme";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private static final String PREFERENCE_DATE_FORMAT = "dateFormat";
    private static final String PREFERENCE_ANIMATIONS = "animations";
    private static final String PREFERENCE_GESTURES = "gestures";
    private static final String PREFERENCE_VOLUME_NAVIGATION = "volumeNavigation";
    private static final String PREFERENCE_MANAGE_BACK = "manage_back";
    private static final String PREFERENCE_START_INTEGRATED_INBOX = "start_integrated_inbox";
    private static final String PREFERENCE_CONFIRM_ACTIONS = "confirm_actions";
    private static final String PREFERENCE_PRIVACY_MODE = "privacy_mode";
    private static final String PREFERENCE_MEASURE_ACCOUNTS = "measure_accounts";
    private static final String PREFERENCE_COUNT_SEARCH = "count_search";
    private static final String PREFERENCE_HIDE_SPECIAL_ACCOUNTS = "hide_special_accounts";
    private static final String PREFERENCE_MESSAGELIST_TOUCHABLE = "messagelist_touchable";
    private static final String PREFERENCE_MESSAGELIST_PREVIEW_LINES = "messagelist_preview_lines";
    private static final String PREFERENCE_MESSAGELIST_STARS = "messagelist_stars";
    private static final String PREFERENCE_MESSAGELIST_CHECKBOXES = "messagelist_checkboxes";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES = "messagelist_show_correspondent_names";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME = "messagelist_show_contact_name";
    private static final String PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR = "messagelist_contact_name_color";
    private static final String PREFERENCE_MESSAGEVIEW_FIXEDWIDTH = "messageview_fixedwidth_font";
    private static final String PREFERENCE_COMPACT_LAYOUTS = "compact_layouts";

    private static final String PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST = "messageview_return_to_list";
    private static final String PREFERENCE_MESSAGEVIEW_ZOOM_CONTROLS_ENABLED = "messageview_zoom_controls";
    private static final String PREFERENCE_QUIET_TIME_ENABLED = "quiet_time_enabled";
    private static final String PREFERENCE_QUIET_TIME_STARTS = "quiet_time_starts";
    private static final String PREFERENCE_QUIET_TIME_ENDS = "quiet_time_ends";


    private static final String PREFERENCE_MESSAGEVIEW_MOBILE_LAYOUT = "messageview_mobile_layout";
    private static final String PREFERENCE_BACKGROUND_OPS = "background_ops";
    private static final String PREFERENCE_GALLERY_BUG_WORKAROUND = "use_gallery_bug_workaround";
    private static final String PREFERENCE_DEBUG_LOGGING = "debug_logging";
    private static final String PREFERENCE_SENSITIVE_LOGGING = "sensitive_logging";

    private static final String PREFERENCE_ATTACHMENT_DEF_PATH = "attachment_default_path";

    private static final int ACTIVITY_CHOOSE_FOLDER = 1;
    private ListPreference mLanguage;
    private ListPreference mTheme;
    private ListPreference mDateFormat;
    private CheckBoxPreference mAnimations;
    private CheckBoxPreference mGestures;
    private CheckBoxListPreference mVolumeNavigation;
    private CheckBoxPreference mManageBack;
    private CheckBoxPreference mStartIntegratedInbox;
    private CheckBoxListPreference mConfirmActions;
    private CheckBoxPreference mPrivacyMode;
    private CheckBoxPreference mMeasureAccounts;
    private CheckBoxPreference mCountSearch;
    private CheckBoxPreference mHideSpecialAccounts;
    private CheckBoxPreference mTouchable;
    private ListPreference mPreviewLines;
    private CheckBoxPreference mStars;
    private CheckBoxPreference mCheckboxes;
    private CheckBoxPreference mShowCorrespondentNames;
    private CheckBoxPreference mShowContactName;
    private CheckBoxPreference mChangeContactNameColor;
    private CheckBoxPreference mFixedWidth;
    private CheckBoxPreference mReturnToList;
    private CheckBoxPreference mZoomControlsEnabled;
    private CheckBoxPreference mMobileOptimizedLayout;
    private ListPreference mBackgroundOps;
    private CheckBoxPreference mUseGalleryBugWorkaround;
    private CheckBoxPreference mDebugLogging;
    private CheckBoxPreference mSensitiveLogging;
    private CheckBoxPreference compactLayouts;

    private CheckBoxPreference mQuietTimeEnabled;
    private jp.co.fttx.rakuphotomail.preferences.TimePickerPreference mQuietTimeStarts;
    private jp.co.fttx.rakuphotomail.preferences.TimePickerPreference mQuietTimeEnds;
    private Preference mAttachmentPathPreference;


    public static void actionPrefs(Context context) {
        Intent i = new Intent(context, Prefs.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.global_preferences);

        mLanguage = (ListPreference) findPreference(PREFERENCE_LANGUAGE);
        Vector<CharSequence> entryVector = new Vector<CharSequence>(Arrays.asList(mLanguage.getEntries()));
        Vector<CharSequence> entryValueVector = new Vector<CharSequence>(Arrays.asList(mLanguage.getEntryValues()));
        String supportedLanguages[] = getResources().getStringArray(R.array.supported_languages);
        HashSet<String> supportedLanguageSet = new HashSet<String>(Arrays.asList(supportedLanguages));
        for (int i = entryVector.size() - 1; i > -1; --i) {
            if (!supportedLanguageSet.contains(entryValueVector.get(i))) {
                entryVector.remove(i);
                entryValueVector.remove(i);
            }
        }
        initListPreference(mLanguage, RakuPhotoMail.getRakuPhotoLanguage(),
                           entryVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY),
                           entryValueVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY));

        final String theme = (RakuPhotoMail.getRakuPhotoTheme() == android.R.style.Theme) ? "dark" : "light";
        mTheme = setupListPreference(PREFERENCE_THEME, theme);

        findPreference(PREFERENCE_FONT_SIZE).setOnPreferenceClickListener(
        new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                onFontSizeSettings();
                return true;
            }
        });

        mDateFormat = (ListPreference) findPreference(PREFERENCE_DATE_FORMAT);
        String[] formats = DateFormatter.getFormats(this);
        CharSequence[] entries = new CharSequence[formats.length];
        CharSequence[] values = new CharSequence[formats.length];
        for (int i = 0 ; i < formats.length; i++) {
            String format = formats[i];
            entries[i] = DateFormatter.getSampleDate(this, format);
            values[i] = format;
        }
        initListPreference(mDateFormat, DateFormatter.getFormat(this), entries, values);

        mAnimations = (CheckBoxPreference)findPreference(PREFERENCE_ANIMATIONS);
        mAnimations.setChecked(RakuPhotoMail.showAnimations());

        mGestures = (CheckBoxPreference)findPreference(PREFERENCE_GESTURES);
        mGestures.setChecked(RakuPhotoMail.gesturesEnabled());

        compactLayouts = (CheckBoxPreference)findPreference(PREFERENCE_COMPACT_LAYOUTS);
        compactLayouts.setChecked(RakuPhotoMail.useCompactLayouts());

        mVolumeNavigation = (CheckBoxListPreference)findPreference(PREFERENCE_VOLUME_NAVIGATION);
        mVolumeNavigation.setItems(new CharSequence[] {getString(R.string.volume_navigation_message), getString(R.string.volume_navigation_list)});
        mVolumeNavigation.setCheckedItems(new boolean[] {RakuPhotoMail.useVolumeKeysForNavigationEnabled(), RakuPhotoMail.useVolumeKeysForListNavigationEnabled()});

        mManageBack = (CheckBoxPreference)findPreference(PREFERENCE_MANAGE_BACK);
        mManageBack.setChecked(RakuPhotoMail.manageBack());

        mStartIntegratedInbox = (CheckBoxPreference)findPreference(PREFERENCE_START_INTEGRATED_INBOX);
        mStartIntegratedInbox.setChecked(RakuPhotoMail.startIntegratedInbox());

        mConfirmActions = (CheckBoxListPreference) findPreference(PREFERENCE_CONFIRM_ACTIONS);
        mConfirmActions.setItems(new CharSequence[] {
                                     getString(R.string.global_settings_confirm_action_delete),
                                     getString(R.string.global_settings_confirm_action_spam),
                                     getString(R.string.global_settings_confirm_action_mark_all_as_read)
                                 });
        mConfirmActions.setCheckedItems(new boolean[] {
                                            RakuPhotoMail.confirmDelete(),
                                            RakuPhotoMail.confirmSpam(),
                                            RakuPhotoMail.confirmMarkAllAsRead()
                                        });

        mPrivacyMode = (CheckBoxPreference) findPreference(PREFERENCE_PRIVACY_MODE);
        mPrivacyMode.setChecked(RakuPhotoMail.keyguardPrivacy());

        mMeasureAccounts = (CheckBoxPreference)findPreference(PREFERENCE_MEASURE_ACCOUNTS);
        mMeasureAccounts.setChecked(RakuPhotoMail.measureAccounts());

        mCountSearch = (CheckBoxPreference)findPreference(PREFERENCE_COUNT_SEARCH);
        mCountSearch.setChecked(RakuPhotoMail.countSearchMessages());

        mHideSpecialAccounts = (CheckBoxPreference)findPreference(PREFERENCE_HIDE_SPECIAL_ACCOUNTS);
        mHideSpecialAccounts.setChecked(RakuPhotoMail.isHideSpecialAccounts());

        mTouchable = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_TOUCHABLE);
        mTouchable.setChecked(RakuPhotoMail.messageListTouchable());

        mPreviewLines = setupListPreference(PREFERENCE_MESSAGELIST_PREVIEW_LINES,
                                            Integer.toString(RakuPhotoMail.messageListPreviewLines()));

        mStars = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_STARS);
        mStars.setChecked(RakuPhotoMail.messageListStars());

        mCheckboxes = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CHECKBOXES);
        mCheckboxes.setChecked(RakuPhotoMail.messageListCheckboxes());

        mShowCorrespondentNames = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES);
        mShowCorrespondentNames.setChecked(RakuPhotoMail.showCorrespondentNames());

        mShowContactName = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME);
        mShowContactName.setChecked(RakuPhotoMail.showContactName());

        mChangeContactNameColor = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR);
        mChangeContactNameColor.setChecked(RakuPhotoMail.changeContactNameColor());
        if (RakuPhotoMail.changeContactNameColor()) {
            mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
        } else {
            mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
        }
        mChangeContactNameColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final Boolean checked = (Boolean) newValue;
                if (checked) {
                    onChooseContactNameColor();
                    mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
                } else {
                    mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
                }
                mChangeContactNameColor.setChecked(checked);
                return false;
            }
        });

        mFixedWidth = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGEVIEW_FIXEDWIDTH);
        mFixedWidth.setChecked(RakuPhotoMail.messageViewFixedWidthFont());

        mReturnToList = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST);
        mReturnToList.setChecked(RakuPhotoMail.messageViewReturnToList());

        mZoomControlsEnabled = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_ZOOM_CONTROLS_ENABLED);
        mZoomControlsEnabled.setChecked(RakuPhotoMail.zoomControlsEnabled());

        mMobileOptimizedLayout = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_MOBILE_LAYOUT);
        if (Integer.parseInt(Build.VERSION.SDK)  <= 7) {
            mMobileOptimizedLayout.setEnabled(false);
        }


        mMobileOptimizedLayout.setChecked(RakuPhotoMail.mobileOptimizedLayout());

        mQuietTimeEnabled = (CheckBoxPreference) findPreference(PREFERENCE_QUIET_TIME_ENABLED);
        mQuietTimeEnabled.setChecked(RakuPhotoMail.getQuietTimeEnabled());

        mQuietTimeStarts = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_STARTS);
        mQuietTimeStarts.setDefaultValue(RakuPhotoMail.getQuietTimeStarts());
        mQuietTimeStarts.setSummary(RakuPhotoMail.getQuietTimeStarts());
        mQuietTimeStarts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String time = (String) newValue;
                mQuietTimeStarts.setSummary(time);
                return false;
            }
        });

        mQuietTimeEnds = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_ENDS);
        mQuietTimeEnds.setSummary(RakuPhotoMail.getQuietTimeEnds());
        mQuietTimeEnds.setDefaultValue(RakuPhotoMail.getQuietTimeEnds());
        mQuietTimeEnds.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String time = (String) newValue;
                mQuietTimeEnds.setSummary(time);
                return false;
            }
        });




        mBackgroundOps = setupListPreference(PREFERENCE_BACKGROUND_OPS, RakuPhotoMail.getBackgroundOps().toString());

        mUseGalleryBugWorkaround = (CheckBoxPreference)findPreference(PREFERENCE_GALLERY_BUG_WORKAROUND);
        mUseGalleryBugWorkaround.setChecked(RakuPhotoMail.useGalleryBugWorkaround());

        mDebugLogging = (CheckBoxPreference)findPreference(PREFERENCE_DEBUG_LOGGING);
        mSensitiveLogging = (CheckBoxPreference)findPreference(PREFERENCE_SENSITIVE_LOGGING);

        mDebugLogging.setChecked(RakuPhotoMail.DEBUG);
        mSensitiveLogging.setChecked(RakuPhotoMail.DEBUG_SENSITIVE);

        mAttachmentPathPreference = findPreference(PREFERENCE_ATTACHMENT_DEF_PATH);
        mAttachmentPathPreference.setSummary(RakuPhotoMail.getAttachmentDefaultPath());
        mAttachmentPathPreference
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileBrowserHelper
                .getInstance()
                .showFileBrowserActivity(Prefs.this,
                                         new File(RakuPhotoMail.getAttachmentDefaultPath()),
                                         ACTIVITY_CHOOSE_FOLDER, callback);

                return true;
            }

            FileBrowserFailOverCallback callback = new FileBrowserFailOverCallback() {

                @Override
                public void onPathEntered(String path) {
                    mAttachmentPathPreference.setSummary(path);
                    RakuPhotoMail.setAttachmentDefaultPath(path);
                }

                @Override
                public void onCancel() {
                    // canceled, do nothing
                }
            };
        });
    }

    private void saveSettings() {
        SharedPreferences preferences = Preferences.getPreferences(this).getPreferences();

        RakuPhotoMail.setK9Language(mLanguage.getValue());
        RakuPhotoMail.setK9Theme(mTheme.getValue().equals("dark") ? android.R.style.Theme : android.R.style.Theme_Light);
        RakuPhotoMail.setAnimations(mAnimations.isChecked());
        RakuPhotoMail.setGesturesEnabled(mGestures.isChecked());
        RakuPhotoMail.setCompactLayouts(compactLayouts.isChecked());
        RakuPhotoMail.setUseVolumeKeysForNavigation(mVolumeNavigation.getCheckedItems()[0]);
        RakuPhotoMail.setUseVolumeKeysForListNavigation(mVolumeNavigation.getCheckedItems()[1]);
        RakuPhotoMail.setManageBack(mManageBack.isChecked());
        RakuPhotoMail.setStartIntegratedInbox(!mHideSpecialAccounts.isChecked() && mStartIntegratedInbox.isChecked());
        RakuPhotoMail.setConfirmDelete(mConfirmActions.getCheckedItems()[0]);
        RakuPhotoMail.setConfirmSpam(mConfirmActions.getCheckedItems()[1]);
        RakuPhotoMail.setConfirmMarkAllAsRead(mConfirmActions.getCheckedItems()[2]);
        RakuPhotoMail.setKeyguardPrivacy(mPrivacyMode.isChecked());
        RakuPhotoMail.setMeasureAccounts(mMeasureAccounts.isChecked());
        RakuPhotoMail.setCountSearchMessages(mCountSearch.isChecked());
        RakuPhotoMail.setHideSpecialAccounts(mHideSpecialAccounts.isChecked());
        RakuPhotoMail.setMessageListTouchable(mTouchable.isChecked());
        RakuPhotoMail.setMessageListPreviewLines(Integer.parseInt(mPreviewLines.getValue()));
        RakuPhotoMail.setMessageListStars(mStars.isChecked());
        RakuPhotoMail.setMessageListCheckboxes(mCheckboxes.isChecked());
        RakuPhotoMail.setShowCorrespondentNames(mShowCorrespondentNames.isChecked());
        RakuPhotoMail.setShowContactName(mShowContactName.isChecked());
        RakuPhotoMail.setChangeContactNameColor(mChangeContactNameColor.isChecked());
        RakuPhotoMail.setMessageViewFixedWidthFont(mFixedWidth.isChecked());
        RakuPhotoMail.setMessageViewReturnToList(mReturnToList.isChecked());
        RakuPhotoMail.setMobileOptimizedLayout(mMobileOptimizedLayout.isChecked());
        RakuPhotoMail.setQuietTimeEnabled(mQuietTimeEnabled.isChecked());

        RakuPhotoMail.setQuietTimeStarts(mQuietTimeStarts.getTime());
        RakuPhotoMail.setQuietTimeEnds(mQuietTimeEnds.getTime());


        RakuPhotoMail.setZoomControlsEnabled(mZoomControlsEnabled.isChecked());
        RakuPhotoMail.setAttachmentDefaultPath(mAttachmentPathPreference.getSummary().toString());
        boolean needsRefresh = RakuPhotoMail.setBackgroundOps(mBackgroundOps.getValue());
        RakuPhotoMail.setUseGalleryBugWorkaround(mUseGalleryBugWorkaround.isChecked());

        if (!RakuPhotoMail.DEBUG && mDebugLogging.isChecked()) {
            Toast.makeText(this, R.string.debug_logging_enabled, Toast.LENGTH_LONG).show();
        }
        RakuPhotoMail.DEBUG = mDebugLogging.isChecked();
        RakuPhotoMail.DEBUG_SENSITIVE = mSensitiveLogging.isChecked();

        Editor editor = preferences.edit();
        RakuPhotoMail.save(editor);
        DateFormatter.setDateFormat(editor, mDateFormat.getValue());
        editor.commit();

        if (needsRefresh) {
            MailService.actionReset(this, null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveSettings();
            if (RakuPhotoMail.manageBack()) {
                //TODO kari
//                Accounts.listAccounts(this);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onFontSizeSettings() {
        FontSizeSettings.actionEditSettings(this);
    }

    private void onChooseContactNameColor() {
        new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                RakuPhotoMail.setContactNameColor(color);
            }
        },
        RakuPhotoMail.getContactNameColor()).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case ACTIVITY_CHOOSE_FOLDER:
            if (resultCode == RESULT_OK && data != null) {
                // obtain the filename
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    String filePath = fileUri.getPath();
                    if (filePath != null) {
                        mAttachmentPathPreference.setSummary(filePath.toString());
                        RakuPhotoMail.setAttachmentDefaultPath(filePath.toString());
                    }
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
