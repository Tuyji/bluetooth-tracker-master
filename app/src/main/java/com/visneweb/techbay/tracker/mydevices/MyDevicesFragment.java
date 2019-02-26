package com.visneweb.techbay.tracker.mydevices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.visneweb.techbay.tracker.R;
import com.visneweb.techbay.tracker.common.MyDeviceAdapter;
import com.visneweb.techbay.tracker.db.AppDatabase;
import com.visneweb.techbay.tracker.db.DeviceDao;
import com.visneweb.techbay.tracker.db.MyDevice;
import com.visneweb.techbay.tracker.service.BluetoothService;

import java.util.List;

/**
 * Created by riskactive on 19.03.2018.
 */

public class MyDevicesFragment extends Fragment {
    private static final int REFRESH_DELAY = 10000;
    private ListView lv;
    private MyDeviceAdapter listAdapter;
    private Handler delayRun = new Handler();
    private Runnable refreshList = new Runnable() {
        @Override
        public void run() {
            listAdapter.refresh();
            delayRun.postDelayed(refreshList, REFRESH_DELAY);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_devices, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        listAdapter = new MyDeviceAdapter() {

            @Override
            public void onCreateextraView(View v, final MyDevice myDevice, int position) {
                ((ToggleButton) v.findViewById(R.id.alarm)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        BluetoothService.getMyConnection(myDevice.getMacAddress()).tweet(isChecked);
                    }
                });
            }

            @Override
            public List<MyDevice> getList() {
                return getRepository().getTracking();
            }

            @Override
            public View onInflateView(ViewGroup root) {
                return LayoutInflater.from(getActivity()).inflate(R.layout.device_item_tweet, root, false);
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
        super.onActivityCreated(savedInstanceState);
    }

    private BluetoothAdapter getBtAdapter() {
        return getBManager().getAdapter();
    }

    private final BluetoothManager getBManager() {
        return (BluetoothManager) getActivity().getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lv = view.findViewById(R.id.list);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList.run();
    }

    private DeviceDao getRepository() {
        return AppDatabase.getAppDatabase(getActivity().getApplicationContext()).deviceDao();
    }
}
