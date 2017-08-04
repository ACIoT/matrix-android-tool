package ablecloud.matrix.tool;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wangkun on 04/08/2017.
 */
public class OtaUpgradeFragment extends DeviceFragment {
    @BindView(R.id.log)
    TextView log;

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
}
