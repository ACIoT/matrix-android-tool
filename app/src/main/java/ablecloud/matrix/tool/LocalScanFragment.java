package ablecloud.matrix.tool;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.local.LocalDevice;
import ablecloud.matrix.local.MatrixLocal;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.functions.Action;

/**
 * Created by wangkun on 07/08/2017.
 */

public class LocalScanFragment extends Fragment {

    private LocalDeviceAdapter adapter;

    @BindView(R.id.swipe)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(android.R.id.list)
    ListView listView;

    @BindView(android.R.id.empty)
    TextView emptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new LocalDeviceAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_scan, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);
        emptyView.setText(getString(R.string.empty_list, getString(R.string.local_device)));
        return view;
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            MatrixLocal.localDeviceManager().findDevice(new MatrixCallback<List<LocalDevice>>() {
                @Override
                public void success(final List<LocalDevice> localDevices) {
                    UiUtils.runOnUiThread(new Action() {
                        @Override
                        public void run() throws Exception {
                            adapter.clear();
                            adapter.addAll(localDevices);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }

                @Override
                public void error(MatrixError matrixError) {
                    UiUtils.toast(getActivity(), "findDevice error: " + matrixError.getMessage());
                    UiUtils.runOnUiThread(new Action() {
                        @Override
                        public void run() throws Exception {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            });
        }
    };

    private static class LocalDeviceAdapter extends ArrayAdapter<LocalDevice> {
        public LocalDeviceAdapter(Context context) {
            super(context, 0);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            LocalDevice localDevice = getItem(position);
            StringBuilder builder = new StringBuilder();
            builder.append("physicalDeviceId: " + localDevice.physicalDeviceId + "\n");
            builder.append("subDomainId: " + localDevice.subDomainId + "\n");
            builder.append("ipAddress: " + localDevice.ipAddress);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(builder.toString());
            return convertView;
        }
    }
}
