package ablecloud.matrix.tool;

import android.app.Activity;
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
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.User;
import ablecloud.matrix.util.PreferencesUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by liuxiaofeng on 2017/10/30.
 */

public class SignInFragment extends Fragment {

    private static final String MATRIX_ACCOUNT = "ablecloud_matrix_account";

    @BindView(R.id.account)
    EditText account;
    @BindView(R.id.password)
    EditText password;
    Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = UiUtils.getSupportActionBar(this);
        actionBar.setTitle(R.string.user_module);
        actionBar.setSubtitle(R.string.login);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.signIn)
    public void onViewClicked() {
        String account = this.account.getText().toString().trim();
        String password = this.password.getText().toString().trim();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            UiUtils.toast(getActivity(), getString(R.string.sign_in_check_tips));
            return;
        }
        Matrix.accountManager().login(account, password, new MatrixCallback<User>() {
            @Override
            public void success(User user) {
                UiUtils.toast(getActivity(), getString(R.string.sign_in_success));
                PreferencesUtils.putString(getActivity(), MATRIX_ACCOUNT, user.phone);
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }

            @Override
            public void error(MatrixError matrixError) {
                UiUtils.toast(getActivity(), matrixError.toString());
            }
        });
    }
}
