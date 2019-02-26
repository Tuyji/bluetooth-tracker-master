package com.visneweb.techbay.tracker.setting;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.visneweb.techbay.tracker.R;

/**
 * Created by riskactive on 16.03.2018.
 */

public class SettingActivity extends PreferenceActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
