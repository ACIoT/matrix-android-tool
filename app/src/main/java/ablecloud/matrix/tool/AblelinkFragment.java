package ablecloud.matrix.tool;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.activator.DeviceType;
import ablecloud.matrix.local.LocalDevice;
import ablecloud.matrix.local.MatrixLocal;
import ablecloud.matrix.util.NetworkUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
import io.reactivex.functions.Action;

/**
 * Created by wangkun on 17/08/2017.
 */

public class AblelinkFragment extends Fragment {

    private Unbinder unbinder;

    @BindView(R.id.ssid)
    EditText ssid;

    @BindView(R.id.password)
    EditText password;

    @BindView(R.id.type_spinner)
    Spinner typeSpinner;

    @BindView(R.id.log)
    TextView log;

    private DeviceType deviceType;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.local_device);
        actionBar.setSubtitle(getString(R.string.ablelink) + " Id: " + MainApplication.getMainDomainId());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MatrixLocal.localDeviceManager().stopAbleLink();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ablelink, container, false);
        unbinder = ButterKnife.bind(this, view);

        ssid.setText(NetworkUtils.getSSID(getActivity()));
        ssid.setKeyListener(null);

        ArrayAdapter<DeviceType> typeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        typeAdapter.addAll(DeviceType.values());
        typeSpinner.setAdapter(typeAdapter);

        log.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnItemSelected(R.id.type_spinner)
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        deviceType = position >= 0 ? DeviceType.values()[position] : null;
    }

    @OnClick(R.id.start_link)
    public void onClick(View v) {
        progressDialog.show();
        MatrixLocal.localDeviceManager().startAblelink(deviceType,
                NetworkUtils.getSSID(getActivity()), password.getText().toString(), (int) TimeUnit.MINUTES.toMillis(1), new MatrixCallback<LocalDevice>() {
                    @Override
                    public void success(final LocalDevice localDevice) {
                        UiUtils.runOnUiThread(new Action() {
                            @Override
                            public void run() throws Exception {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                if (log != null) {
                                    log.append(log.length() > 0 ? "\n---\n" : "");
                                    log.append("ip: " + localDevice.ipAddress + ", physicalDeviceId: " + localDevice.physicalDeviceId + "\n");
                                }
                            }
                        });
                    }

                    @Override
                    public void error(final MatrixError matrixError) {
                        UiUtils.runOnUiThread(new Action() {
                            @Override
                            public void run() throws Exception {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                if (log != null) {
                                    log.append(log.length() > 0 ? "\n---\n" : "");
                                    log.append("Ablelink error: " + matrixError.getMessage());
                                }
                            }
                        });
                    }
                });
    }
}
