package ablecloud.matrix.tool;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.model.User;
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
 * Created by liuxiaofeng on 2017/10/30.
 */

public class SignUpFragment extends Fragment {

    @BindView(R.id.phone)
    EditText phone;
    @BindView(R.id.verifyCode)
    EditText verifyCode;
    @BindView(R.id.password)
    EditText password;
    Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.user_module);
        actionBar.setSubtitle(R.string.sign_up);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.fetchVerCode, R.id.signUp})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fetchVerCode:
                fetchVerCode();
                break;
            case R.id.signUp:
                signUp();
                break;
        }
    }

    private void fetchVerCode() {
        final String phone = this.phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            UiUtils.toast(getActivity(), getString(R.string.type_your_phone));
            return;
        }
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                ablecloud.matrix.app.Matrix.accountManager().requireVerifyCode(phone, 0, new MatrixCallback<Void>() {
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
                        UiUtils.toast(getActivity(), getString(R.string.ver_code_sent));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        UiUtils.toast(getActivity(), throwable.toString());
                    }
                });

    }

    private void signUp() {
        final String phone = this.phone.getText().toString().trim();
        final String verCode = verifyCode.getText().toString().trim();
        final String password = this.password.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(verCode) || TextUtils.isEmpty(password)) {
            UiUtils.toast(getActivity(), getString(R.string.sign_up_check_tips));
            return;
        }

        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(final CompletableEmitter e) throws Exception {
                ablecloud.matrix.app.Matrix.accountManager().register(phone, "", password, verCode, "", new MatrixCallback<User>() {
                    @Override
                    public void success(User user) {
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
                        UiUtils.toast(getActivity(), getString(R.string.sign_up_done));
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
