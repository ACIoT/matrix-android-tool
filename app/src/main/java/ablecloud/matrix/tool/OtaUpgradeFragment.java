package ablecloud.matrix.tool;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.UpgradeRequest;
import ablecloud.matrix.model.VersionQuery;
import ablecloud.matrix.model.VersionResponse;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wangkun on 04/08/2017.
 */
public class OtaUpgradeFragment extends DeviceFragment {
    @BindView(R.id.log)
    TextView log;
    private String targetVersion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.device_management);
        actionBar.setSubtitle(R.string.ota_upgrade);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ota_upgrade, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.checkVersionMcu, R.id.confirmUpgradeMcu, R.id.checkVersionWifi, R.id.confirmUpgradeWifi})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkVersionMcu:
                checkVersion(VersionQuery.TYPE_MCU);
                break;
            case R.id.confirmUpgradeMcu:
                confirmUpgrade(VersionQuery.TYPE_MCU);
                break;
            case R.id.checkVersionWifi:
                checkVersion(VersionQuery.TYPE_WIFI);
                break;
            case R.id.confirmUpgradeWifi:
                confirmUpgrade(VersionQuery.TYPE_WIFI);
                break;
        }
    }

    private void checkVersion(final int otaType) {
        final VersionQuery versionRequest = new VersionQuery(device.subDomainName, otaType);
        log("checkVersion: " + device.physicalDeviceId);
        Single.create(new SingleOnSubscribe<VersionResponse>() {
            @Override
            public void subscribe(final SingleEmitter<VersionResponse> e) throws Exception {
                Matrix.otaManager().checkVersion(versionRequest, new MatrixCallback<VersionResponse>() {
                    @Override
                    public void success(VersionResponse versionResponse) {
                        e.onSuccess(versionResponse);
                    }

                    @Override
                    public void error(final MatrixError matrixError) {
                        e.onError(matrixError);
                    }
                });
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<VersionResponse>() {
                    @Override
                    public void accept(VersionResponse versionResponse) throws Exception {
                        String type = otaType == VersionQuery.TYPE_MCU ? "MCU" : "WIFI";
                        final StringBuilder builder = new StringBuilder();
                        builder.append("OTA type: " + type + ", ");
                        boolean hasUpgrade = versionResponse.hasUpgrade();
                        builder.append("hasUpgrade: " + hasUpgrade + "\n");
                        builder.append("current: " + versionResponse.currentVersion);
                        if (hasUpgrade) {
                            targetVersion = versionResponse.targetVersion;
                            builder.append(", target: " + targetVersion);
                        } else {
                            targetVersion = null;
                        }
                        log(builder.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        UiUtils.toast(getActivity(), throwable.getMessage());
                    }
                });

    }

    private void confirmUpgrade(int otaType) {
        final UpgradeRequest upgradeRequest = new UpgradeRequest(device.subDomainName, device.deviceId, targetVersion, otaType);
        log("confirmUpgrade: " + device.physicalDeviceId + ", targetVersion: " + targetVersion);
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                Matrix.otaManager().confirmUpgrade(upgradeRequest, new MatrixCallback<Void>() {
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
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        UiUtils.toast(getActivity(), "confirmUpgrade success");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        UiUtils.toast(getActivity(), "confirmUpgrade error: " + throwable.getMessage());
                    }
                });

    }

    private void log(final String string) {
        Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                log.append(log.length() > 0 ? "---\n" : "");
                log.append(string);
                log.append("\n");
            }
        });
    }

}
