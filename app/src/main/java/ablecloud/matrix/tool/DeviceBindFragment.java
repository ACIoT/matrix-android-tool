package ablecloud.matrix.tool;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
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
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
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
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                Matrix.bindManager().bindDevice(subDomain.getText().toString(), physicalId.getText().toString(), devieName.getText().toString(), new MatrixCallback<Device>() {
                    @Override
                    public void success(Device device) {
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
                        UiUtils.toast(getActivity(), "bindDevice success");
                        getActivity().finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        UiUtils.toast(getActivity(), throwable.toString());
                    }
                });

    }
}
