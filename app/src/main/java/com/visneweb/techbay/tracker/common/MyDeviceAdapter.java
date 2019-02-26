package com.visneweb.techbay.tracker.common;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.visneweb.techbay.tracker.db.MyDevice;

/**
 * Created by aureo on 16.02.2018.
 */

public abstract class MyDeviceAdapter extends AbstractAdapter<MyDevice> {


    @Override
    public void onCreateViewHolder(View v, MyDevice myDevice, int position) {
        myDevice = getItem(position);
        TextView name = v.findViewById(com.visneweb.techbay.tracker.R.id.name);
        if (myDevice.getDisplayName() != null) {
            name.setText(myDevice.getDisplayName());
        } else if (myDevice.getName() != null) {
            name.setText(myDevice.getName());
        }
        Switch track = v.findViewById(com.visneweb.techbay.tracker.R.id.track);
        track.setChecked(myDevice.isTracked());
        track.setOnCheckedChangeListener(getListener(myDevice));
        track.setChecked(myDevice.isTracked());
        onCreateextraView(v, myDevice, position);
    }

    public abstract void onCreateextraView(View v, MyDevice myDevice, int position);
}
