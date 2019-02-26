package com.visneweb.techbay.tracker.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.visneweb.techbay.tracker.db.MyDevice;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

/**
 * Created by riskactive on 20.03.2018.
 */

public abstract class MyConnection extends BluetoothGattCallback {
    public static final int WEAK = -90;
    public static final int MEDIUM = -75;
    public static final int STRONG = -60;
    public static final int WARNING_DELAY = 10000;
    private static final int RECONNECT_DELAY = 10000;
    private static final UUID BUTTON_PRESSED_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static final UUID TWEET_CHARACTERISTIC_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    private static final UUID BUTTON_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID TWEET_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    private static byte[] HIGH_ALARM_VALUE = new byte[]{
            (byte) 2
    };
    private static byte[] STOP_ALARM_VALUE = new byte[]{
            (byte) 0
    };
    private boolean canWarn = true;
    private boolean connected = false;
    private BluetoothGatt gatt;
    private MyDevice myDevice;
    private Uri imageUri;
    private BluetoothGattCharacteristic tweetCharacteristic;
    private Runnable letWarn = new Runnable() {
        @Override
        public void run() {
            canWarn = true;
        }
    };
    private Handler delayRun = new Handler();
    private Runnable dontWarnForAWhile = new Runnable() {
        @Override
        public void run() {
            canWarn = false;
            delayRun.postDelayed(letWarn, WARNING_DELAY);
        }
    };

    public MyConnection(MyDevice d) {
        myDevice = d;
    }

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public String getDeviceAddress() {
        return myDevice.getMacAddress();
    }

    public void tweet(boolean isChecked) {
        Log.i("BLT", "tries to tweet the device..");
        if (gatt == null) {
            Log.i("BLT", "but gatt is null!");
        }
        if (tweetCharacteristic == null) {
            gatt.connect();
            Log.i("BLT", "tweet service is still null");
            return;
        }
        byte[] value;
        if (isChecked) {
            value = HIGH_ALARM_VALUE;
        } else {
            value = STOP_ALARM_VALUE;
        }
        tweetCharacteristic.setValue(value);
        gatt.writeCharacteristic(tweetCharacteristic);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (canWarn) {
            if (rssi < WEAK) {
                warn(true);
                dontWarnForAWhile.run();
            } else if (rssi < MEDIUM) {
                warn(false);
                dontWarnForAWhile.run();

            } else if (rssi > STRONG) {
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            if (service.getUuid().equals(BUTTON_SERVICE_UUID)) {
                gatt.setCharacteristicNotification(service.getCharacteristic(BUTTON_PRESSED_UUID), true);
            } else if (service.getUuid().equals(TWEET_SERVICE_UUID)) {
                tweetCharacteristic = service.getCharacteristics().get(0);
//                gatt.setCharacteristicNotification(service.getCharacteristic(TWEET_CHARACTERISTIC_UUID),true);
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(BUTTON_PRESSED_UUID)) {
            takePhoto();
        }
    }

    public void takePhoto() {
        startCameraIntent();
    }

    public abstract void startCameraIntent();

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i("BLT", "status: " + status + " new state: " + newState);
        if (newState == STATE_CONNECTED) {
            gatt.discoverServices();
            connected = true;
        } else if (newState == STATE_DISCONNECTED) {
            connected = false;
        }
    }

    private void warn(boolean tooFar) {
        notify(tooFar, gatt);
    }

    public abstract void notify(boolean tooFar, BluetoothGatt gatt);

    public void check() {
        if (connected) {
            gatt.readRemoteRssi();
        }
    }
}
