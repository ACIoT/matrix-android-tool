package ablecloud.matrix.tool;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.sloydev.preferator.Preferator;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.User;
import ablecloud.matrix.util.PreferencesUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends ContainerActivity {

    private static final String MATRIX_ACCOUNT = "ablecloud_matrix_account";

    @BindView(R.id.bottom_bar)
    BottomNavigationView bottomBar;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_bottom_bar;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@android.support.annotation.NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.local:
                        replaceFragment(LocalFragment.class, false);
                        return true;
                    case R.id.cloud:
                        replaceFragment(CloudFragment.class, false);
                        return true;
                    case R.id.others:
                        replaceFragment(OthersFragment.class, false);
                        return true;
                }
                return false;
            }
        });
        bottomBar.setSelectedItemId(R.id.local);
    }

    public static class LocalFragment extends PreferenceFragment {

        private String title;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_local);
            title = getString(R.string.local);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            UiUtils.getSupportActionBar(this)
                    .setSubtitle(getString(R.string.main_domain_id) + ": " + MainApplication.getMainDomainId());
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()) {
                case "ablelink":
                    FunctionActivity.showFragment(getActivity(), AblelinkFragment.class.getName(), title);
                    return true;
                case "APlink":
                    FunctionActivity.showFragment(getActivity(), APlinkFragment.class.getName(), title);
                    return true;
                case "local_scan":
                    FunctionActivity.showFragment(getActivity(), LocalScanFragment.class.getName(), title);
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    public static class CloudFragment extends PreferenceFragment {
        private AlertDialog loginDialog;
        private AlertDialog logoutDialog;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.fragment_cloud);

            loginDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("登录")
                    .setView(R.layout.dialog_login)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String a = ((TextView) loginDialog.findViewById(R.id.account)).getText().toString();
                            String p = ((TextView) loginDialog.findViewById(R.id.password)).getText().toString();
                            login(a, p).observeOn(AndroidSchedulers.mainThread()).doFinally(new Action() {
                                @Override
                                public void run() throws Exception {
                                    getActivity().invalidateOptionsMenu();
                                }
                            }).subscribe(new Consumer<User>() {
                                @Override
                                public void accept(@NonNull User user) throws Exception {
                                    PreferencesUtils.putString(getActivity(), MATRIX_ACCOUNT, a);
                                    Toast.makeText(getActivity(), "登录成功", Toast.LENGTH_SHORT).show();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(@NonNull Throwable throwable) throws Exception {
                                    Toast.makeText(getActivity(), "登录失败:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            logoutDialog = new AlertDialog.Builder(getActivity())
                    .setMessage("确认退出？")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Matrix.accountManager().logout();
                            getActivity().invalidateOptionsMenu();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        private Single<User> login(final String account, final String password) {
            return Single.create(new SingleOnSubscribe<User>() {
                @Override
                public void subscribe(final SingleEmitter<User> emitter) throws Exception {
                    Matrix.accountManager().login(account, password, new MatrixCallback<User>() {
                        @Override
                        public void success(User user) {
                            emitter.onSuccess(user);
                        }

                        @Override
                        public void error(MatrixError error) {
                            emitter.onError(error);
                        }
                    });
                }
            });
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            UiUtils.getSupportActionBar(this)
                    .setSubtitle(getString(R.string.main_domain) + ": " + MainApplication.getMainDomain());
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.fragment_cloud, menu);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            MenuItem accountItem = menu.findItem(R.id.account);
            boolean login = Matrix.accountManager().isLogin();
            if (!login) {
                accountItem.setTitle(R.string.login);
            } else {
                accountItem.setTitle(PreferencesUtils.getString(getActivity(), MATRIX_ACCOUNT));
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.account:
                    if (Matrix.accountManager().isLogin()) {
                        if (!logoutDialog.isShowing())
                            logoutDialog.show();
                    } else {
                        if (!loginDialog.isShowing()) {
                            loginDialog.show();
                        }
                    }
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (!Matrix.accountManager().isLogin()) {
                Toast.makeText(getActivity(), "Need login first", Toast.LENGTH_SHORT).show();
                return false;
            }

            switch (preference.getKey()) {
                case "device_bind":
                    FunctionActivity.showFragment(getActivity(), DeviceBindFragment.class.getName(), getString(R.string.device_management));
                    return true;
                case "ota_upgrade":
                    FunctionActivity.showFragment(getActivity(), OtaUpgradeFragment.class.getName(), getString(R.string.device_management));
                    return true;
                case "cloud_message":
                    FunctionActivity.showFragment(getActivity(), CloudMessageFragment.class.getName(), getString(R.string.device_control));
                    return true;
                case "subscribe":
                    FunctionActivity.showFragment(getActivity(), SubscribeFragment.class.getName(), getString(R.string.device_data));
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    public static class OthersFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_others);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            UiUtils.getSupportActionBar(this).setSubtitle(R.string.others);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()) {
                case "settings":
                    Preferator.launch(getActivity());
                    getActivity().finish();
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
