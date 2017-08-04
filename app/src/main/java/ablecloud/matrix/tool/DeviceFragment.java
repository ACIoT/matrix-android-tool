package ablecloud.matrix.tool;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import ablecloud.matrix.helper.DeviceManager;
import ablecloud.matrix.model.Device;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;

/**
 * Created by wangkun on 04/08/2017.
 */

public class DeviceFragment extends Fragment {
    private ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {
        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            device = navigationAdapter.getItem(itemPosition);
            return true;
        }
    };

    private DataSetObserver deviceObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action() {
                @Override
                public void run() throws Exception {
                    navigationAdapter.clear();
                    navigationAdapter.addAll(DeviceManager.getDevices());
                }
            });
        }
    };

    private DeviceNavigationAdapter navigationAdapter;
    protected Device device;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationAdapter = new DeviceNavigationAdapter(getActivity());
        navigationAdapter.addAll(DeviceManager.getDevices());
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(navigationAdapter, navigationListener);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        DeviceManager.registerDataSetObserver(deviceObserver);
        DeviceManager.updateDevices();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DeviceManager.unregisterDataSetObserver(deviceObserver);
    }

    private static class DeviceNavigationAdapter extends ArrayAdapter<Device> {
        public DeviceNavigationAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView textView = (TextView) view;
            Device device = getItem(position);
            textView.setText(device.physicalDeviceId);
            boolean cloudOnline = (device.status & Device.CLOUD_ONLINE) != 0;
            int colorRes = cloudOnline ? android.R.color.holo_green_light : android.R.color.holo_red_light;
            textView.setTextColor(ContextCompat.getColor(getContext(), colorRes));
            return view;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }
}
