package com.visneweb.techbay.tracker.scan;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import com.visneweb.techbay.tracker.common.MyDeviceCreateListener;
import com.visneweb.techbay.tracker.db.MyDevice;

/**
 * Created by riskactive on 27.03.2018.
 */

public abstract class MyDeviceResolver implements MyDeviceCreateListener {


    public void resolve(BluetoothGatt gatt) {
        BluetoothDevice bDevice = gatt.getDevice();
        String name = bDevice.getName();
        String address = bDevice.getAddress();
        MyDevice myDevice = new MyDevice();
        myDevice.setMacAddress(address);
        myDevice.setName(name);
        onMyDeviceCreated(myDevice);
    }
}
