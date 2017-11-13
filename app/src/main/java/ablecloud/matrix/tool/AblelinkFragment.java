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
import ablecloud.matrix.MatrixReceiver;
import ablecloud.matrix.activator.DeviceActivator;
import ablecloud.matrix.activator.DeviceType;
import ablecloud.matrix.local.AbleLinkingFind;
import ablecloud.matrix.local.LocalDevice;
import ablecloud.matrix.local.LocalDeviceManager;
import ablecloud.matrix.local.MatrixLocal;
import ablecloud.matrix.util.NetworkUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

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
        final int timeout_ms = (int) TimeUnit.MINUTES.toMillis(1);
        final PublishSubject<LocalDevice> subject = PublishSubject.create();
        final DeviceActivator deviceActivator = DeviceActivator.of(deviceType);
        final AbleLinkingFind ableLinkingFind = new AbleLinkingFind(timeout_ms, LocalDeviceManager.DEFAULT_INTERVAL_MS, new MatrixReceiver<LocalDevice>() {
            @Override
            public void onReceive(LocalDevice localDevice) {
                subject.onNext(localDevice);
            }
        });

        Observable<LocalDevice> deviceObservable = subject.doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                deviceActivator.startAbleLink(NetworkUtils.getSSID(getActivity()), password.getText().toString(), timeout_ms);
                ableLinkingFind.execute(new MatrixCallback<Void>() {
                    @Override
                    public void success(Void aVoid) {
                        subject.onComplete();
                    }

                    @Override
                    public void error(MatrixError matrixError) {
                        subject.onError(matrixError);
                    }
                });
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                ableLinkingFind.cancel();
                deviceActivator.stopAblelink();
            }
        }).subscribeOn(Schedulers.io());

        deviceObservable.observeOn(AndroidSchedulers.mainThread()).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(@NonNull Disposable disposable) throws Exception {
                progressDialog.show();
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                progressDialog.cancel();
            }
        }).subscribe(new Consumer<LocalDevice>() {
            @Override
            public void accept(@NonNull LocalDevice localDevice) throws Exception {
                if (log != null) {
                    log.append(log.length() > 0 ? "\n---\n" : "");
                    log.append("ip: " + localDevice.ipAddress + ", physicalDeviceId: " + localDevice.physicalDeviceId + "\n");
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                if (log != null) {
                    log.append(log.length() > 0 ? "\n---\n" : "");
                    log.append("Ablelink error: " + throwable.getMessage());
                }
            }
        });
    }
}
