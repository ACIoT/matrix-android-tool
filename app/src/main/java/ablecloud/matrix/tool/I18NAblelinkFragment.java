package ablecloud.matrix.tool;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.activator.DeviceType;
import ablecloud.matrix.local.LocalDevice;
import ablecloud.matrix.local.MatrixLocal;
import ablecloud.matrix.local.SmartModeNetConfigManager;
import ablecloud.matrix.util.NetworkUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wangkun on 17/08/2017.
 */

public class I18NAblelinkFragment extends Fragment {

    private Unbinder unbinder;

    @BindView(R.id.ssid)
    EditText ssid;

    @BindView(R.id.password)
    EditText password;

    @BindView(R.id.type_spinner)
    Spinner typeSpinner;
    @BindView(R.id.checkbox)
    CheckBox mCheckBox;
    @BindView(R.id.log)
    TextView log;

    private DeviceType deviceType;
    private Snackbar progressSnack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setSubtitle(getString(R.string.ablelink) + " Id: " + MainApplication.getMainDomainId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MatrixLocal.localDeviceManager().stopAbleLink();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ablelink_i18n, container, false);
        unbinder = ButterKnife.bind(this, view);

        ssid.setText(NetworkUtils.getSSID(getActivity()));
        ssid.setKeyListener(null);

        ArrayAdapter<DeviceType> typeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        typeAdapter.addAll(DeviceType.values());
        typeSpinner.setAdapter(typeAdapter);

        log.setMovementMethod(new ScrollingMovementMethod());

        progressSnack = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) progressSnack.getView();
        LayoutInflater.from(snackbarLayout.getContext()).inflate(R.layout.snack_progress, snackbarLayout);
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
        final int timeout_ms = (int) TimeUnit.MINUTES.toMillis(1);


        Disposable subscribe = Single.create(new SingleOnSubscribe<LocalDevice>() {
            @Override
            public void subscribe(final SingleEmitter<LocalDevice> e) throws Exception {
                MatrixLocal.smartModeNetConfigManager().configNetType(mCheckBox.isChecked() ? SmartModeNetConfigManager.NET.LOCAL : SmartModeNetConfigManager.NET.CLOUD).startAblelink(deviceType, NetworkUtils.getSSID(getActivity()), password.getText().toString(), timeout_ms, new MatrixCallback<LocalDevice>() {
                    @Override
                    public void success(LocalDevice localDevice) {
                        e.onSuccess(localDevice);
                    }

                    @Override
                    public void error(MatrixError matrixError) {
                        e.onError(matrixError);
                    }
                });
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        progressSnack.show();
                    }
                })
                .timeout(60, TimeUnit.SECONDS)
                .subscribe(new Consumer<LocalDevice>() {
                    @Override
                    public void accept(LocalDevice localDevice) throws Exception {
                        progressSnack.dismiss();
                        if (log != null) {
                            log.append(log.length() > 0 ? "\n---\n" : "");
                            log.append("ip: " + localDevice.ipAddress + ", physicalDeviceId: " + localDevice.physicalDeviceId + "\n");
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        progressSnack.dismiss();
                        if (log != null) {
                            log.append(log.length() > 0 ? "\n---\n" : "");
                            log.append("Ablelink error: " + throwable.getMessage());
                        }
                    }
                });
    }
}
