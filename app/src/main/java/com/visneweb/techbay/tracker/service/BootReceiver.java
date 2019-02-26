package com.visneweb.techbay.tracker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

/**
 * Created by aureo on 27.02.2018.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> addresses = pref.getStringSet("devices", null);
        if (addresses != null) {
            for (String address : addresses) {
                Intent signalService = new Intent(context, BluetoothService.class);
                signalService.putExtra("address", address);
                context.startService(signalService);
            }
        }
    }
}
