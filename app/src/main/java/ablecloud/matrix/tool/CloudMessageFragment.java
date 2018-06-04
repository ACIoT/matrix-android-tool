package ablecloud.matrix.tool;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.MatrixMessage;
import ablecloud.matrix.app.BindManager;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okio.ByteString;

/**
 * Created by wangkun on 02/08/2017.
 */

public class CloudMessageFragment extends DeviceFragment {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    @BindView(R.id.msgCode)
    EditText msgCode;

    @BindView(R.id.request)
    EditText request;

    @BindView(R.id.log)
    TextView log;

    private int cursor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.device_control);
        actionBar.setSubtitle(R.string.cloud_message);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cloud_message, container, false);
        ButterKnife.bind(this, view);
        log.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }

    @OnTextChanged(R.id.request)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (before != count) {
            cursor = start + count;
        } else {
            request.setSelection(cursor);
        }
    }

    @OnClick(R.id.send)
    public void onClick(View v) {
        String msgCode = this.msgCode.getText().toString().trim();
        final String requestMessage = request.getText().toString();
        final long requestTime = System.currentTimeMillis();
        if (TextUtils.isEmpty(msgCode) || TextUtils.isEmpty(requestMessage)) {
            Toast.makeText(getActivity(), "msgCode和payload均不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Single.create(new SingleOnSubscribe<MatrixMessage>() {
            @Override
            public void subscribe(final SingleEmitter<MatrixMessage> e) throws Exception {
                Matrix.bindManager()
                        .sendDevice(device.subDomainName, device.physicalDeviceId, BindManager.Mode.CLOUD_ONLY, new MatrixMessage(Integer.parseInt(CloudMessageFragment.this.msgCode.getText().toString().trim()), ByteString.decodeHex(requestMessage).toByteArray()), new MatrixCallback<MatrixMessage>() {
                            @Override
                            public void success(MatrixMessage deviceMessage) {
                                e.onSuccess(deviceMessage);
                            }

                            @Override
                            public void error(MatrixError matrixError) {
                                e.onError(matrixError);
                            }
                        });
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MatrixMessage>() {
                    @Override
                    public void accept(MatrixMessage matrixMessage) throws Exception {
                        Single.just(matrixMessage.getContent()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<byte[]>() {
                            @Override
                            public void accept(@NonNull byte[] bytes) throws Exception {
                                log.append(log.length() > 0 ? "\n---\n" : "");
                                log.append(formatTime(requestTime) + ": Send: " + requestMessage + "\n");
                                log.append(formatTime(System.currentTimeMillis()) + ": Receive: " + ByteString.of(bytes).hex());
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (throwable instanceof MatrixError) {
                            MatrixError matrixError = (MatrixError) throwable;

                            Single.just(matrixError).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<MatrixError>() {
                                @Override
                                public void accept(@NonNull MatrixError matrixError) throws Exception {
                                    Toast.makeText(getActivity(), matrixError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

    }

    private String formatTime(long time) {
        return FORMAT.format(new Date(time));
    }
}
