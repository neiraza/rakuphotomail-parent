package jp.co.fttx.rakuphotomail.activity;

import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.preference.Preference;


public class RakuphotoPreferenceActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle icicle) {
        RakuPhotoActivity.setLanguage(this, RakuPhotoMail.getRakuPhotoLanguage());
        // http://code.google.com/p/k9mail/issues/detail?id=2439
        // Re-enable themeing support in preferences when
        // http://code.google.com/p/android/issues/detail?id=4611 is resolved
        // setTheme(K9.getK9Theme());
        super.onCreate(icicle);
    }

    /**
     * Set up the {@link ListPreference} instance identified by {@code key}.
     *
     * @param key   The key of the {@link ListPreference} object.
     * @param value Initial value for the {@link ListPreference} object.
     * @return The {@link ListPreference} instance identified by {@code key}.
     */
    protected ListPreference setupListPreference(final String key, final String value) {
        final ListPreference prefView = (ListPreference) findPreference(key);
        prefView.setValue(value);
        prefView.setSummary(prefView.getEntry());
        prefView.setOnPreferenceChangeListener(new PreferenceChangeListener(prefView));
        return prefView;
    }

    /**
     * Initialize a given {@link ListPreference} instance.
     *
     * @param prefView    The {@link ListPreference} instance to initialize.
     * @param value       Initial value for the {@link ListPreference} object.
     * @param entries     Sets the human-readable entries to be shown in the list.
     * @param entryValues The array to find the value to save for a preference when an
     *                    entry from entries is selected.
     */
    protected void initListPreference(final ListPreference prefView, final String value,
                                      final CharSequence[] entries, final CharSequence[] entryValues) {
        prefView.setEntries(entries);
        prefView.setEntryValues(entryValues);
        prefView.setValue(value);
        prefView.setSummary(prefView.getEntry());
        prefView.setOnPreferenceChangeListener(new PreferenceChangeListener(prefView));
    }

    /**
     * This class handles value changes of the {@link ListPreference} objects.
     */
    private static class PreferenceChangeListener implements Preference.OnPreferenceChangeListener {
        private ListPreference mPrefView;

        private PreferenceChangeListener(final ListPreference prefView) {
            this.mPrefView = prefView;
        }

        /**
         * Show the preference value in the preference summary field.
         */
        @Override
        public boolean onPreferenceChange(final Preference preference, final Object newValue) {
            final String summary = newValue.toString();
            final int index = mPrefView.findIndexOfValue(summary);
            mPrefView.setSummary(mPrefView.getEntries()[index]);
            mPrefView.setValue(summary);
            return false;
        }
    }

}
