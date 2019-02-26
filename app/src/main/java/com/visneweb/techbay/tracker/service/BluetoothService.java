package com.visneweb.techbay.tracker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.visneweb.techbay.tracker.MyPreferenceManager;
import com.visneweb.techbay.tracker.R;
import com.visneweb.techbay.tracker.camera.CameraActivity;
import com.visneweb.techbay.tracker.common.MainActivity;
import com.visneweb.techbay.tracker.db.AppDatabase;
import com.visneweb.techbay.tracker.db.DeviceDao;
import com.visneweb.techbay.tracker.db.MyDevice;

import java.util.ArrayList;
import java.util.List;

import static com.visneweb.techbay.tracker.common.Constants.ACTION;
import static com.visneweb.techbay.tracker.common.Constants.ACTION_START_CAMERA;
import static com.visneweb.techbay.tracker.common.Constants.ACTION_TAKE_PHOTO;


public class BluetoothService extends Service {
    private static final int TAKE_PICTURE = 1;


    private static final int SIGNAL_DELAY = 1000;
    private static List<MyConnection> signals = new ArrayList<>();
    private final Handler delayRun = new Handler();
    BluetoothAdapter btAdapter;
    private NotificationManager manager;
    private Runnable startCheck = new Runnable() {
        @Override
        public void run() {
            checkAll();
            delayRun.postDelayed(startCheck, SIGNAL_DELAY);
        }
    };

    public static MyConnection getMyConnection(String address) {
        MyConnection answer = null;
        for (MyConnection con : signals) {
            if (con.getDeviceAddress().equals(address)) {
                answer = con;
            }
        }
        return answer;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onDestroy() {
        manager.cancel(R.string.app_name);
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BluetoothGatt getGatt(MyDevice d, MyConnection s) {
        Log.i("BLT", "creating new gatt for new Signal");
        BluetoothDevice bd = btAdapter.getRemoteDevice(d.getMacAddress());
        return bd.connectGatt(this, true, s);
    }

    private void removeUnselectSignals(List<MyDevice> myDevices) {
        for (int i = 0; i < signals.size(); i++) {
            boolean found = false;
            for (MyDevice d : myDevices) {
                if (signals.get(i).getDeviceAddress().equals(d.getMacAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Log.i("BLT", "undesired signal is removed");
                signals.remove(i);
            }
        }
    }

    private void addNewSelectedSignals(List<MyDevice> myDevices) {
        for (final MyDevice d : myDevices) {
            boolean found = false;
            for (MyConnection s : signals) {
                if (s.getDeviceAddress().equals(d.getMacAddress())) {
                    Log.i("BLT", "device is alrady in signal list");
                    found = true;
                    break;
                }
            }
            if (!found) {
                Log.i("BLT", "new device requested to be tracked, creating new signal");
                MyConnection newSignal = new MyConnection(d) {
                    @Override
                    public void startCameraIntent() {
                        if (CameraActivity.isRunning) {
                            Intent intent = new Intent();
                            intent.setAction(ACTION_TAKE_PHOTO);
                            sendBroadcast(intent);
                        } else {
                            Intent cam = new Intent(getApplicationContext(), CameraActivity.class);
                            cam.putExtra(ACTION, ACTION_START_CAMERA);
                            cam.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(cam);
                        }
                    }

                    @Override
                    public void notify(boolean tooFar, BluetoothGatt gatt) {
                        sendNotification(d, tooFar);
                    }
                };
                newSignal.setGatt(getGatt(d, newSignal));
                signals.add(newSignal);
            }
        }
    }

    private void updateSignals() {
        List<MyDevice> myDevices = getRepository().getTracking();
        if (myDevices == null || myDevices.isEmpty()) {
            signals = new ArrayList<>();
            return;
        }
        if (signals.size() > 0) {
            removeUnselectSignals(myDevices);
        }
        if (signals.size() < myDevices.size()) {
            addNewSelectedSignals(myDevices);
        }
    }

    private DeviceDao getRepository() {
        return AppDatabase.getAppDatabase(this).deviceDao();
    }

    @Override
    public void onCreate() {
        Log.i("BLT", "service is created");
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        startCheck.run();
    }

    private void sendNotification(MyDevice d, boolean tooFar) {
        manager.notify(R.string.app_name, getNotification(d, tooFar));
    }

    private Notification getNotification(MyDevice d, boolean tooFar) {
        Uri sound;
        String title;
        String detail;
        if (tooFar) {
            sound = MyPreferenceManager.getPref(this).getAlarmUri();
            title = getString(R.string.alarm_title);
            detail = getString(R.string.alarm_detail, d.getName());
        } else {
            sound = MyPreferenceManager.getPref(this).getWarningUri();
            title = getString(R.string.warning_title, d.getName());
            detail = getString(R.string.warning_detail, d.getName());
        }
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_logo, getString(R.string.click_for_detail), pi);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, d.getMacAddress())
                .setContentTitle(title)
                .setContentInfo(detail)
                .setSound(sound)
                .setSmallIcon(R.drawable.ic_logo)
                .addAction(action);
        return notification.build();
    }

    private void checkAll() {
        updateSignals();
        for (MyConnection s : signals) {
            s.check();
        }
    }
}