package ablecloud.matrix.tool;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.local.LocalDeviceManager;
import ablecloud.matrix.local.MatrixLocal;
import ablecloud.matrix.local.MatrixWifiInfo;
import ablecloud.matrix.util.MatrixUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by liuxiaofeng on 2017/11/14.
 */

public class APlinkFragment extends Fragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private WifiListAdapter wifiListAdapter;
    public static final int LOCAL_TIMEOUT_MS = 8000;
    private static final int APLINK_DEFAULT_TIMEOUT_MS = 60 * 1000;
    private LocalDeviceManager manager = MatrixLocal.localDeviceManager();
    private Snackbar progressSnack;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_aplink, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        progressSnack = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) progressSnack.getView();
        LayoutInflater.from(snackbarLayout.getContext()).inflate(R.layout.snack_progress, snackbarLayout);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.device_ap_confirm))
                .show();
        wifiListAdapter = new WifiListAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(wifiListAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((FunctionActivity) getActivity()).getSupportActionBar().setSubtitle(getString(R.string.wifi_list));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.get_wifi_list)
    public void onViewClicked() {
        Observable
                .create(new ObservableOnSubscribe<List<MatrixWifiInfo>>() {
                    @Override
                    public void subscribe(final ObservableEmitter<List<MatrixWifiInfo>> e) throws Exception {
                        manager.getWifiFromAP(LOCAL_TIMEOUT_MS, new MatrixCallback<List<MatrixWifiInfo>>() {
                            @Override
                            public void success(List<MatrixWifiInfo> matrixWifiInfos) {
                                e.onNext(matrixWifiInfos);
                                e.onComplete();
                            }

                            @Override
                            public void error(MatrixError matrixError) {
                                e.onError(matrixError);
                                e.onComplete();
                            }
                        });
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        progressSnack.setText(R.string.loading_available_wifi_list);
                        progressSnack.show();
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        progressSnack.dismiss();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<MatrixWifiInfo>>() {
                    @Override
                    public void accept(@NonNull List<MatrixWifiInfo> matrixWifiInfos) throws Exception {
                        if (wifiListAdapter != null) {
                            wifiListAdapter.setData(matrixWifiInfos);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        UiUtils.toast(getActivity(), throwable.toString());
                    }
                });
    }


    private void setWifiToAP(final String ssid, final String wifiPwd) {
        MatrixUtils.assertNotEmpty(getActivity(), "ssid", ssid);
        MatrixUtils.assertNotEmpty(getActivity(), "wifiPwd", wifiPwd);
        Observable
                .create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(final ObservableEmitter<Boolean> e) throws Exception {
                        manager.setWifiToAP(ssid, wifiPwd, APLINK_DEFAULT_TIMEOUT_MS, new MatrixCallback<Void>() {
                            @Override
                            public void success(Void aVoid) {
                                e.onNext(true);
                                e.onComplete();
                            }

                            @Override
                            public void error(MatrixError matrixError) {
                                e.onError(matrixError);
                            }
                        });
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        progressSnack.setText(R.string.ap_linking);
                        progressSnack.show();
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        progressSnack.dismiss();
                    }
                })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean success) throws Exception {
                        UiUtils.toast(getActivity(), getString(R.string.ap_link_success));
                        getActivity().finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        UiUtils.toast(getActivity(), throwable.getMessage().toString());
                    }
                });
    }

    private class WifiListAdapter extends RecyclerView.Adapter<VH> {

        private List<MatrixWifiInfo> wifiInfos = new ArrayList<>();

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_wifi_list, parent, false);
            return new VH(itemView);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            final MatrixWifiInfo wifiInfo = wifiInfos.get(position);
            holder.ssid.setText(wifiInfo.ssid);
            holder.sendSsidPwd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_send_ssid_pwd, null);
                    ((TextInputEditText) dialogView.findViewById(R.id.ssid)).setText(wifiInfo.ssid);
                    final TextInputEditText wifiPwd = dialogView.findViewById(R.id.wifi_pwd);
                    new AlertDialog.Builder(APlinkFragment.this.getActivity())
                            .setView(dialogView)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    setWifiToAP(wifiInfo.ssid, wifiPwd.getText().toString().trim());
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return wifiInfos.size();
        }

        public void setData(List<MatrixWifiInfo> data) {
            this.wifiInfos = data;
            notifyDataSetChanged();
        }
    }

    private static class VH extends RecyclerView.ViewHolder {

        public final TextView ssid;
        public final Button sendSsidPwd;

        public VH(View itemView) {
            super(itemView);
            ssid = itemView.findViewById(R.id.ssid);
            sendSsidPwd = itemView.findViewById(R.id.send_ssid_pwd);
        }
    }
}
