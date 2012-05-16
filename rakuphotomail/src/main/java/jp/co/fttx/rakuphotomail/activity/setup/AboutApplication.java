/*
 * Copyright (c) 2012, UCOM Corporation and/or its affiliates. All rights reserved.
 * UCOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package jp.co.fttx.rakuphotomail.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.util.Log;
import jp.co.fttx.rakuphotomail.R;
import jp.co.fttx.rakuphotomail.RakuPhotoMail;
import jp.co.fttx.rakuphotomail.activity.RakuphotoPreferenceActivity;

public class AboutApplication extends RakuphotoPreferenceActivity {

    private PreferenceScreen mVersion;

    private static final String PREFERENCE_ABOUT_APP_RELEASE_VERSION = "about_app_release_version";

    public static void actionSettings(Context context) {
        Intent i = new Intent(context, AboutApplication.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_application_preferences);
        mVersion = (PreferenceScreen) findPreference(PREFERENCE_ABOUT_APP_RELEASE_VERSION);

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getString(R.string.app_package), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(RakuPhotoMail.LOG_TAG, e.getMessage());
        }
        mVersion.setSummary(packageInfo.versionName);


    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
