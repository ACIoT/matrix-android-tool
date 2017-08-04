package ablecloud.matrix.tool;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import ablecloud.matrix.DeviceMessage;
import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.BindManager;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.helper.DeviceManager;
import ablecloud.matrix.model.Device;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import okio.ByteString;

/**
 * Created by wangkun on 02/08/2017.
 */

public class CloudMessageFragment extends Fragment {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final Pattern LINKIFY_HEX = Pattern.compile("(^|\\s+)([\\da-fA-F]{2})+($|\\s+)");

    @BindView(R.id.request)
    EditText request;

    @BindView(R.id.record)
    TextView record;

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
    private Device device;
    private int cursor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigationAdapter = new DeviceNavigationAdapter(getActivity());
        navigationAdapter.addAll(DeviceManager.getDevices());
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setSubtitle(R.string.cloud_message);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(navigationAdapter, navigationListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_control, container, false);
        ButterKnife.bind(this, view);
        record.setMovementMethod(new ScrollingMovementMethod());
        return view;
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

    @OnTextChanged(R.id.request)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (before != count) {
            cursor = start + count;
            Linkify.addLinks(request, LINKIFY_HEX, null);
        } else {
            request.setSelection(cursor);
        }
    }

    @OnClick(R.id.send)
    public void onClick(View v) {
        final String requestMessage = request.getText().toString();
        final long requestTime = System.currentTimeMillis();
        Matrix.bindManager().sendDevice(device.subDomainName, device.physicalDeviceId, BindManager.Mode.CLOUD_ONLY, new DeviceMessage(66, ByteString.decodeHex(requestMessage).toByteArray()), new MatrixCallback<DeviceMessage>() {
            @Override
            public void success(DeviceMessage deviceMessage) {
                Single.just(deviceMessage.getContent()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(@NonNull byte[] bytes) throws Exception {
                        record.append(record.length() > 0 ? "\n---\n" : "");
                        record.append(formatTime(requestTime) + ": Send: " + requestMessage + "\n");
                        record.append(formatTime(System.currentTimeMillis()) + ": Receive: " + ByteString.of(bytes).hex());
                        Linkify.addLinks(record, LINKIFY_HEX, null);
                    }
                });
            }

            @Override
            public void error(MatrixError matrixError) {
                Single.just(matrixError).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<MatrixError>() {
                    @Override
                    public void accept(@NonNull MatrixError matrixError) throws Exception {
                        Toast.makeText(getActivity(), matrixError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String formatTime(long time) {
        return FORMAT.format(new Date(time));
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
