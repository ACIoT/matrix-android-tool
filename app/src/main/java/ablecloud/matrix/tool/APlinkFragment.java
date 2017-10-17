package ablecloud.matrix.tool;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.local.LocalDevice;
import ablecloud.matrix.local.LocalDeviceManager;
import ablecloud.matrix.local.MatrixLocal;
import ablecloud.matrix.local.MatrixWifiInfo;
import ablecloud.matrix.util.NetworkUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.functions.Functions;

/**
 * Created by wangkun on 17/10/2017.
 */

public class APlinkFragment extends Fragment {

    public static final int LOCAL_TIMEOUT_MS = 8000;

    private Unbinder unbinder;

    @BindView(R.id.ssid)
    EditText ssidEdit;

    @BindView(R.id.password)
    EditText passwordEdit;

    @BindView(R.id.log)
    TextView log;

    private LocalDeviceManager manager = MatrixLocal.localDeviceManager();
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aplink, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.local_device);
        actionBar.setSubtitle(getString(R.string.aplink) + " Id: " + MainApplication.getMainDomainId());
        UiUtils.toast(getActivity(), R.string.device_ap_confirm);
    }

    @OnClick(R.id.start_link)
    public void onClick(View v) {
        final long timeout = TimeUnit.SECONDS.toMillis(60);
        final String ssid = ssidEdit.getText().toString();
        final String password = passwordEdit.getText().toString();
        confirmWifiFromAP(ssid)
                .andThen(setWifiToAP(ssid, password))
                .andThen(connectWifi(ssid, password, TimeUnit.SECONDS.toMillis(30)))
                .subscribe(findLinkingDevice((int) timeout));
    }

    private Completable setWifiToAP(final String ssid, final String password) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                manager.setWifiToAP(ssid, password, LOCAL_TIMEOUT_MS, new MatrixCallback<Void>() {
                    @Override
                    public void success(Void aVoid) {
                        e.onComplete();
                    }

                    @Override
                    public void error(MatrixError matrixError) {
                        e.onError(matrixError);
                    }
                });
            }
        });
    }

    private Completable confirmWifiFromAP(final String ssid) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                manager.getWifiFromAP(LOCAL_TIMEOUT_MS, new MatrixCallback<List<MatrixWifiInfo>>() {
                    @Override
                    public void success(List<MatrixWifiInfo> matrixWifiInfos) {
                        for (MatrixWifiInfo wifiInfo : matrixWifiInfos) {
                            if (wifiInfo.ssid.equals(ssid)) {
                                e.onComplete();
                                return;
                            }
                        }
                        e.onError(new Exception("AP device not found WIFI " + ssid));
                    }

                    @Override
                    public void error(MatrixError error) {
                        e.onError(error);
                    }
                });
            }
        });
    }

    private Completable connectWifi(final String ssid, final String password, final long timeout_ms) {
        return Completable.complete().doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                NetworkUtils.configWPAWifi(getActivity(), ssid, password);
            }
        }).andThen(Observable.timer(1000, TimeUnit.MILLISECONDS)).takeUntil(new Predicate<Long>() {
            @Override
            public boolean test(@NonNull Long aLong) throws Exception {
                return ssid.equals(NetworkUtils.getSSID(getActivity()));
            }
        }).flatMapCompletable(Functions.justFunction(Completable.complete())).timeout(timeout_ms, TimeUnit.MILLISECONDS);
    }

    private Action findLinkingDevice(final int timeout) {
        return new Action() {
            @Override
            public void run() throws Exception {
                manager.findLinkingDevice(timeout, LocalDeviceManager.DEFAULT_INTERVAL_MS, new MatrixCallback<LocalDevice>() {

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
                    public void error(final MatrixError error) {
                        UiUtils.runOnUiThread(new Action() {
                            @Override
                            public void run() throws Exception {
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }
                                if (log != null) {
                                    log.append(log.length() > 0 ? "\n---\n" : "");
                                    log.append("Ablelink error: " + error.getMessage());
                                }
                            }
                        });
                    }
                });
            }
        };
    }
}
