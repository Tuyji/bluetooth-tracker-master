package com.visneweb.techbay.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Created by riskactive on 21.03.2018.
 */

public class MyPreferenceManager {
    public static String ALARM_SOUND = "ringtone_alarm";
    public static String WARNING_SOUND = "ringtone_warning";
    private static MyPreferenceManager instance;
    private static SharedPreferences p;

    private MyPreferenceManager(Context c) {
        p = PreferenceManager.getDefaultSharedPreferences(c);
    }

    public static MyPreferenceManager getPref(Context c) {
        if (instance == null) {
            instance = new MyPreferenceManager(c);
        }
        return instance;
    }

    public Uri getAlarmUri() {
        String s = p.getString(ALARM_SOUND, null);
        return getUri(s);
    }

    public Uri getWarningUri() {
        String s = p.getString(WARNING_SOUND, null);
        return getUri(s);
    }

    private Uri getUri(String s) {
        if (s == null) {
            return null;
        }
        return Uri.parse(s);
    }
}
