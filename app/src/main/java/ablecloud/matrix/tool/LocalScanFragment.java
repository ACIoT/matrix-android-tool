package ablecloud.matrix.tool;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.local.LocalDevice;
import ablecloud.matrix.local.LocalDeviceManager;
import ablecloud.matrix.local.MatrixLocal;
import ablecloud.matrix.model.Device;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

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

    private boolean linking;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new LocalDeviceAdapter(getActivity());
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_scan, container, false);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);
        emptyView.setText(getString(R.string.empty_list, getString(R.string.local)));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.local);
        actionBar.setSubtitle(getString(R.string.local_scan) + " Id: " + MainApplication.getMainDomainId());
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            adapter.clear();

            if (linking) {
                UiUtils.toast(getActivity(), R.string.scan_linking);
                MatrixLocal.localDeviceManager().findLinkingDevice(LocalDeviceManager.DEFAULT_TIMEOUT_MS, LocalDeviceManager.DEFAULT_INTERVAL_MS, new MatrixCallback<LocalDevice>() {
                    @Override
                    public void success(final LocalDevice localDevice) {
                        UiUtils.runOnUiThread(new Action() {
                            @Override
                            public void run() throws Exception {
                                adapter.add(localDevice);
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void error(MatrixError matrixError) {
                        UiUtils.toast(getActivity(), "findLinkingDevice error: " + matrixError.getMessage());
                        UiUtils.runOnUiThread(new Action() {
                            @Override
                            public void run() throws Exception {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                });
                return;
            } else {
                UiUtils.toast(getActivity(), R.string.scan_all);
                MatrixLocal.localDeviceManager().findDevice(new MatrixCallback<List<LocalDevice>>() {
                    @Override
                    public void success(final List<LocalDevice> localDevices) {
                        UiUtils.runOnUiThread(new Action() {
                            @Override
                            public void run() throws Exception {
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
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_local_scan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.linking);
        item.setTitle(linking ? R.string.scan_linking : R.string.scan_all);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.linking:
                linking = !linking;
                getActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
            final LocalDevice localDevice = getItem(position);
            StringBuilder builder = new StringBuilder();
            builder.append("physicalDeviceId: " + localDevice.physicalDeviceId + "\n");
            builder.append("subDomainId: " + localDevice.subDomainId + "\n");
            builder.append("ipAddress: " + localDevice.ipAddress);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(builder.toString());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ensureLoginAndBind(localDevice, new Consumer<Device>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Device device) throws Exception {
                            Bundle arguments = new Bundle();
                            arguments.putString("subDomain", device.subDomainName);
                            arguments.putString("physicalDeviceId", device.physicalDeviceId);
                            LocalMessageFragment localMessageFragment = new LocalMessageFragment();
                            localMessageFragment.setArguments(arguments);
                            ((FunctionActivity) getContext()).replaceFragment(localMessageFragment);
                        }
                    });
                }
            });
            return convertView;
        }

        private void ensureLoginAndBind(final LocalDevice localDevice, Consumer consumer) {
            Observable
                    .create(new ObservableOnSubscribe<Boolean>() {
                        @Override
                        public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                            checkLogin(emitter);
                        }
                    })
                    .flatMapSingle(new Function<Boolean, SingleSource<?>>() {
                        @Override
                        public SingleSource<?> apply(@io.reactivex.annotations.NonNull Boolean aBoolean) throws Exception {
                            return Single.create(new SingleOnSubscribe<Object>() {
                                @Override
                                public void subscribe(SingleEmitter<Object> emitter) throws Exception {
                                    checkBind(emitter, localDevice);
                                }
                            });
                        }
                    })
                    .subscribe(consumer, new Consumer<Throwable>() {
                        @Override
                        public void accept(@io.reactivex.annotations.NonNull Throwable throwable) throws Exception {
                            UiUtils.toast(getContext(), throwable.getMessage());
                        }
                    });
        }

        private void checkLogin(ObservableEmitter<Boolean> emitter) {
            boolean login = Matrix.accountManager().isLogin();
            if (login) {
                emitter.onNext(true);
            } else {
                emitter.onError(new MatrixError(100, "Need login first"));
            }
        }

        private void checkBind(final SingleEmitter<Object> emitter, final LocalDevice localDevice) {
            Matrix.bindManager().listDevices(new MatrixCallback<List<Device>>() {
                @Override
                public void success(List<Device> devices) {
                    for (Device device : devices) {
                        if (device.physicalDeviceId.equals(localDevice.physicalDeviceId)) {
                            emitter.onSuccess(device);
                            return;
                        }
                    }
                    emitter.onError(new MatrixError(101, "Need bind first"));
                }

                @Override
                public void error(MatrixError matrixError) {
                    emitter.onError(matrixError);
                }
            });
        }
    }
}
