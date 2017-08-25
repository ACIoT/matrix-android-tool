package ablecloud.matrix.tool;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.DeviceDataManager;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.Device;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by liuxiaofeng on 25/08/2017.
 */

public class SubscribeFragment extends Fragment {

    @BindView(R.id.device_list)
    RecyclerView deviceList;
    @BindView(R.id.logcat)
    TextView logcat;
    Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Matrix.deviceDataManager().registerPropertyReceiver(propertyReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscribe, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logcat.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchDevices();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Matrix.deviceDataManager().unregisterPropertyReceiver(propertyReceiver);
    }

    private void fetchDevices() {
        Matrix.bindManager().listDevices(new MatrixCallback<List<Device>>() {
            @Override
            public void success(List<Device> devices) {
                showDevices(devices);
            }

            @Override
            public void error(MatrixError matrixError) {
                UiUtils.toast(getActivity(), getString(R.string.list_devices_error) + "\n" + matrixError.toString());
            }
        });
    }

    private void showDevices(final List<Device> devices) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeviceListAdapter adapter = new DeviceListAdapter(devices);
                deviceList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                deviceList.setAdapter(adapter);
            }
        });
    }

    private DeviceDataManager.PropertyReceiver propertyReceiver = new DeviceDataManager.PropertyReceiver() {
        @Override
        public void onPropertyReceive(String subDomain, long deviceId, String property) {
            String time = Calendar.getInstance().getTime().toLocaleString() + "\n";
            String whichDevice = "[subDomain:" + subDomain + ",deviceId:" + deviceId + "]\n";
            logcat.append(time + whichDevice + property + "\n" + "\n");
        }
    };

    private static class DeviceListAdapter extends RecyclerView.Adapter<DeviceListItemVH> {

        private List<Device> devices = new ArrayList<>();

        public DeviceListAdapter(List<Device> devices) {
            this.devices = devices;
        }

        @Override

        public DeviceListItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
            return new DeviceListItemVH(itemView);
        }

        @Override
        public void onBindViewHolder(final DeviceListItemVH holder, int position) {
            final Device device = devices.get(position);
            holder.physicalDeviceId.setText(device.physicalDeviceId);
            boolean cloudOnline = (device.status & Device.CLOUD_ONLINE) != 0;
            int colorRes = cloudOnline ? android.R.color.holo_green_light : android.R.color.holo_red_light;
            holder.physicalDeviceId.setTextColor(ContextCompat.getColor(holder.physicalDeviceId.getContext(), colorRes));
            holder.subscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Matrix.deviceDataManager().subscribeProperty(device.subDomainName, device.deviceId, new MatrixCallback<Void>() {
                        @Override
                        public void success(Void aVoid) {
                            holder.itemView.setActivated(true);
                        }

                        @Override
                        public void error(MatrixError matrixError) {
                        }
                    });
                }
            });
            holder.unSubscribe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Matrix.deviceDataManager().unsubscribeProperty(device.subDomainName, device.deviceId, new MatrixCallback<Void>() {
                        @Override
                        public void success(Void aVoid) {
                            holder.itemView.setActivated(false);
                        }

                        @Override
                        public void error(MatrixError matrixError) {
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

    }

    private static class DeviceListItemVH extends RecyclerView.ViewHolder {

        public TextView physicalDeviceId;
        public Button subscribe;
        public Button unSubscribe;

        public DeviceListItemVH(View itemView) {
            super(itemView);
            this.physicalDeviceId = (TextView) itemView.findViewById(R.id.physical_device_id);
            this.subscribe = (Button) itemView.findViewById(R.id.subscribe);
            this.unSubscribe = (Button) itemView.findViewById(R.id.unsubscribe);
        }

    }
}
