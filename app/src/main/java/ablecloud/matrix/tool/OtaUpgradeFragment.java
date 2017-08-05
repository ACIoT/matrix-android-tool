package ablecloud.matrix.tool;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.app.OTAManager;
import ablecloud.matrix.model.UpgradeRequest;
import ablecloud.matrix.model.VersionRequest;
import ablecloud.matrix.model.VersionResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

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
        ActionBar actionBar = getActivity().getActionBar();
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
                checkVersion(OTAManager.TYPE_MCU);
                break;
            case R.id.confirmUpgradeMcu:
                confirmUpgrade(OTAManager.TYPE_MCU);
                break;
            case R.id.checkVersionWifi:
                checkVersion(OTAManager.TYPE_WIFI);
                break;
            case R.id.confirmUpgradeWifi:
                confirmUpgrade(OTAManager.TYPE_WIFI);
                break;
        }
    }

    private void checkVersion(final int otaType) {
        VersionRequest versionRequest = new VersionRequest(device.subDomainName, device.deviceId, otaType);
        log("checkVersion: " + device.physicalDeviceId);
        Matrix.otaManager().checkVersion(versionRequest, new MatrixCallback<VersionResponse>() {
            @Override
            public void success(VersionResponse versionResponse) {
                String type = otaType == OTAManager.TYPE_MCU ? "MCU" : "WIFI";
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

            @Override
            public void error(final MatrixError matrixError) {
                toast(matrixError.getMessage());
            }
        });
    }

    private void confirmUpgrade(int otaType) {
        UpgradeRequest upgradeRequest = new UpgradeRequest(device.subDomainName, device.deviceId, targetVersion, otaType);
        log("confirmUpgrade: " + device.physicalDeviceId + ", targetVersion: " + targetVersion);
        Matrix.otaManager().confirmUpgrade(upgradeRequest, new MatrixCallback<Void>() {
            @Override
            public void success(Void aVoid) {
                toast("confirmUpgrade success");
            }

            @Override
            public void error(MatrixError matrixError) {
                toast("confirmUpgrade error: " + matrixError.getMessage());
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

    private void toast(final String message) {
        Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
