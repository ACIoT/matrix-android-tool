package ablecloud.matrix.tool;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import ablecloud.matrix.DeviceMessage;
import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.BindManager;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import okio.ByteString;

/**
 * Created by liuxiaofeng on 01/09/2017.
 */

public class LocalMessageFragment extends Fragment {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private static final Pattern LINKIFY_HEX = Pattern.compile("(^|\\s+)([\\da-fA-F]{2})+($|\\s+)");

    @BindView(R.id.msgCode)
    EditText msgCode;
    @BindView(R.id.request)
    EditText request;
    @BindView(R.id.log)
    TextView log;
    Unbinder unbinder;
    private String subDomain;
    private String physicalDeviceId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subDomain = getArguments().getString("subDomain");
        physicalDeviceId = getArguments().getString("physicalDeviceId");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_message, container, false);
        unbinder = ButterKnife.bind(this, view);
        log.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.local_message);
        actionBar.setSubtitle(getString(R.string.physical_device_id, physicalDeviceId));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.send)
    public void onViewClicked() {
        String msgCode = this.msgCode.getText().toString().trim();
        final String requestMessage = request.getText().toString();
        final long requestTime = System.currentTimeMillis();
        if (TextUtils.isEmpty(msgCode) || TextUtils.isEmpty(requestMessage)) {
            Toast.makeText(getActivity(), "msgCode和payload均不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        DeviceMessage deviceMessage = new DeviceMessage(Integer.parseInt(msgCode), ByteString.decodeHex(requestMessage).toByteArray());
        Matrix.bindManager().sendDevice(subDomain, physicalDeviceId, BindManager.Mode.LOCAL_ONLY, deviceMessage, new MatrixCallback<DeviceMessage>() {
            @Override
            public void success(DeviceMessage deviceMessage) {
                Single.just(deviceMessage.getContent()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(@NonNull byte[] bytes) throws Exception {
                        log.append(log.length() > 0 ? "\n---\n" : "");
                        log.append(formatTime(requestTime) + ": Send: " + requestMessage + "\n");
                        log.append(formatTime(System.currentTimeMillis()) + ": Receive: " + ByteString.of(bytes).hex());
                        Linkify.addLinks(log, LINKIFY_HEX, null);
                    }
                });
            }

            @Override
            public void error(MatrixError matrixError) {
                Single.just(matrixError).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<MatrixError>() {
                    @Override
                    public void accept(@NonNull MatrixError error) throws Exception {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String formatTime(long time) {
        return FORMAT.format(new Date(time));
    }
}
