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
        String phone = this.phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            UiUtils.toast(getActivity(), getString(R.string.type_your_phone));
            return;
        }
        ablecloud.matrix.app.Matrix.accountManager().requireVerifyCode(phone, 0, new MatrixCallback<Void>() {
            @Override
            public void success(Void aVoid) {
                UiUtils.toast(getActivity(), getString(R.string.ver_code_sent));
            }

            @Override
            public void error(MatrixError matrixError) {
                UiUtils.toast(getActivity(), matrixError.toString());
            }
        });
    }

    private void signUp() {
        String phone = this.phone.getText().toString().trim();
        String verCode = verifyCode.getText().toString().trim();
        String password = this.password.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(verCode) || TextUtils.isEmpty(password)) {
            UiUtils.toast(getActivity(), getString(R.string.sign_up_check_tips));
            return;
        }
        ablecloud.matrix.app.Matrix.accountManager().register(phone, "", password, verCode, "", new MatrixCallback<User>() {
            @Override
            public void success(User user) {
                UiUtils.toast(getActivity(), getString(R.string.sign_up_done));
                getActivity().finish();
            }

            @Override
            public void error(MatrixError matrixError) {
                UiUtils.toast(getActivity(), matrixError.toString());
            }
        });
    }
}
