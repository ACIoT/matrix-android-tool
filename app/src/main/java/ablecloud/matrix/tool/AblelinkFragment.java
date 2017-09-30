package ablecloud.matrix.tool;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import butterknife.Unbinder;
import io.reactivex.functions.Action;

/**
 * Created by wangkun on 17/08/2017.
 */

public class AblelinkFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private Unbinder unbinder;

    @BindView(R.id.ssid)
    TextView ssid;

    @BindView(R.id.password)
    EditText password;

    @BindView(R.id.type_group)
    RadioGroup typeGroup;

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

        ssid.setText(getString(R.string.ssid_label, NetworkUtils.getSSID(getActivity())));

        DeviceType[] types = DeviceType.values();
        for (int i = 0; i < types.length; i++) {
            RadioButton button = new RadioButton(getActivity());
            button.setId(R.id.type_group + i + 1);
            button.setText(types[i].name());
            typeGroup.addView(button);
        }
        typeGroup.setOnCheckedChangeListener(this);
        typeGroup.check(R.id.type_group + 1);

        log.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        deviceType = DeviceType.values()[checkedId - R.id.type_group - 1];
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
