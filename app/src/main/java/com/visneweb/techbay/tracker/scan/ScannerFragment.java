package com.visneweb.techbay.tracker.scan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.visneweb.techbay.tracker.R;
import com.visneweb.techbay.tracker.common.MyDeviceAdapter;
import com.visneweb.techbay.tracker.db.AppDatabase;
import com.visneweb.techbay.tracker.db.DeviceDao;
import com.visneweb.techbay.tracker.db.MyDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by riskactive on 16.03.2018.
 */

public class ScannerFragment extends Fragment {
    private static final String ITAG = "iTAG            ";
    private static final long SCAN_PERIOD = 600000;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    private BluetoothAdapter btAdapter;
    private ScanSettings settings;
    private SwitchCompat scan;
    private BluetoothGatt gatt;
    private MyDeviceAdapter listAdapter;
    private ListView lv;
    private ArrayList<ScanFilter> filters;
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();
            if (!alreadyInList(btDevice)) {
                Log.i("BLT", "device name #" + btDevice.getName() + "#");
                upsertToDB(btDevice);
                listAdapter.refresh();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            btAdapter = null;
            mLEScanner = null;
            Log.e("BLT", "Error Code: " + errorCode);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scanner, container, false);
    }

    private BluetoothAdapter getBtAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getAdapter();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getActivity(), "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        listAdapter = new MyDeviceAdapter() {

            @Override
            public void onCreateextraView(View v, MyDevice myDevice, int position) {

            }

            @Override
            public List<MyDevice> getList() {
                List<MyDevice> list = getRepository().getNearDevices();
                return list;
            }

            @Override
            public View onInflateView(ViewGroup root) {
                return LayoutInflater.from(getActivity()).inflate(R.layout.bluetooth_item, root, false);
            }

            @Override
            public CompoundButton.OnCheckedChangeListener getListener(final MyDevice myDevice) {
                return new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        MyDevice newDevice = myDevice;
                        if (isChecked) {
                            newDevice.setTracked(true);
                        } else {
                            newDevice.setTracked(false);
                        }
                        getRepository().update(newDevice);
                    }
                };
            }
        };
        lv.setAdapter(listAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        scan = view.findViewById(com.visneweb.techbay.tracker.R.id.scan);
        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    clearList();
                    startScanning();
                } else {
                    stopScan();
                }
            }
        });
        lv = view.findViewById(R.id.list);
    }

    @Override
    public void onDestroy() {
        if (gatt == null) {
            super.onDestroy();
            return;
        }
        gatt.close();
        gatt = null;
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                getActivity().finish();
            } else if (resultCode == Activity.RESULT_OK) {
                onResume();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean alreadyInList(BluetoothDevice d) {
        for (MyDevice myDevice : getRepository().getNearDevices()) {
            if (myDevice.getMacAddress() == d.getAddress()) {
                return true;
            }
        }
        return false;
    }

    private void upsertToDB(BluetoothDevice bd) {
        MyDevice d = getMyDeviceFromDB(bd);
        if (d == null) {
            d = new MyDevice();
            d.setNear(true);
            d.setMacAddress(bd.getAddress());
            d.setName(bd.getName());
            d.setTracked(false);
            getRepository().insert(d);
        } else {
            getRepository().setNear(d.getMacAddress());
        }
        listAdapter.refresh();
    }

    private MyDevice getMyDeviceFromDB(BluetoothDevice bd) {
        return getRepository().findDevice(bd.getAddress());
    }

    private void startScanning() {
        Log.i("BLT", "start scanning");
        if (btAdapter == null) {
            btAdapter = getBtAdapter();
        }
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            scan.setChecked(false);
            return;
        }
        if (mLEScanner == null) {
            mLEScanner = btAdapter.getBluetoothLeScanner();
        }
        if (settings == null) {
            ScanSettings.Builder builder = new ScanSettings.Builder();
            settings = builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        }
        if (filters == null) {
            ScanFilter.Builder builder = new ScanFilter.Builder();
            builder.setDeviceName(ITAG);
            filters = new ArrayList<>();
            filters.add(builder.build());
        }
        mLEScanner.startScan(filters, settings, mScanCallback);
    }

    private void stopScan() {
        Log.i("BLT", "stop scanning");
        if (mLEScanner != null) {
            mLEScanner.stopScan(mScanCallback);
        }
        btAdapter = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (btAdapter != null) {
            scan.setChecked(false);
        }
    }

    private void clearList() {
        getRepository().setAllFar();
        listAdapter.refresh();
    }

    private DeviceDao getRepository() {
        return AppDatabase.getAppDatabase(getActivity().getApplicationContext()).deviceDao();
    }

    @Override
    public void onResume() {
        super.onResume();
        btAdapter = getBtAdapter();
        if (btAdapter == null) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scan.setChecked(true);
        }
    }
}
