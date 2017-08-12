package ablecloud.matrix.tool;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.Device;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liuxiaofeng on 12/08/2017.
 */

public class DeviceBindFragment extends Fragment {

    @BindView(R.id.subDomain)
    EditText subDomain;

    @BindView(R.id.physicalId)
    EditText physicalId;

    @BindView(R.id.devieName)
    EditText devieName;

    Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(R.string.device_management);
        actionBar.setSubtitle(R.string.device_bind);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_bind, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.bind)
    public void onViewClicked() {
        Matrix.bindManager().bindDevice(subDomain.getText().toString(), physicalId.getText().toString(), devieName.getText().toString(), new MatrixCallback<Device>() {
            @Override
            public void success(Device device) {
                UiUtils.toast(getActivity(), "bindDevice success");
            }

            @Override
            public void error(MatrixError matrixError) {
                UiUtils.toast(getActivity(), matrixError.toString());
            }
        });
    }
}
