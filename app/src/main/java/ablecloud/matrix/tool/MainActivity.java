package ablecloud.matrix.tool;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;

import com.sloydev.preferator.Preferator;

import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.util.PreferencesUtils;
import ablecloud.matrix.util.UiUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

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
        public static final String TAG = "LocalFragment";
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
                case "global_able_link":
                    FunctionActivity.showFragment(getActivity(), I18NAblelinkFragment.class.getName(), title);
                    return true;
                case "global_ap_link":
                    FunctionActivity.showFragment(getActivity(), I18NAPlinkFragment.class.getName(), title);
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    public static class CloudFragment extends PreferenceFragment {
        public static final String TAG = "CloudFragment";
        public static final int REQUEST_CODE_LOGIN = 100;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
            addPreferencesFromResource(R.xml.fragment_cloud);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            UiUtils.getSupportActionBar(this).setSubtitle(getString(R.string.main_domain) + ": " + MainApplication.getMainDomain());
            String account = PreferencesUtils.getString(getActivity(), MATRIX_ACCOUNT);
            findPreference("sign_in").setTitle(Matrix.accountManager().isLogin() ? getString(R.string.who_login, account) : getString(R.string.login));
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

            switch (preference.getKey()) {
                case "sign_in":
                    if (Matrix.accountManager().isLogin()) {
                        UiUtils.toast(getActivity(), getString(R.string.logout_first));
                        return true;
                    }
                    FunctionActivity.showFragmentForResult(this, SignInFragment.class.getName(), getString(R.string.user_module));
                    return true;
                case "sign_out":
                    if (!Matrix.accountManager().isLogin()) {
                        UiUtils.toast(getActivity(), getString(R.string.login_first));
                        return true;
                    }
                    Matrix.accountManager().logout();
                    UiUtils.toast(getActivity(), getString(R.string.sign_out_success));
                    findPreference("sign_in").setTitle(getString(R.string.login));
                    return true;
                case "sign_up":
                    if (Matrix.accountManager().isLogin()) {
                        UiUtils.toast(getActivity(), getActivity().getString(R.string.logout_first));
                        return true;
                    }
                    FunctionActivity.showFragment(getActivity(), SignUpFragment.class.getName(), getString(R.string.user_module));
                    return true;
                case "device_bind":
                    if (ensureLogin()) {
                        FunctionActivity.showFragment(getActivity(), DeviceBindFragment.class.getName(), getString(R.string.device_management));
                    }
                    return true;
                case "ota_upgrade":
                    if (ensureLogin()) {
                        FunctionActivity.showFragment(getActivity(), OtaUpgradeFragment.class.getName(), getString(R.string.device_management));
                    }
                    return true;
                case "cloud_message":
                    if (ensureLogin()) {
                        FunctionActivity.showFragment(getActivity(), CloudMessageFragment.class.getName(), getString(R.string.device_control));
                    }
                    return true;
                case "subscribe":
                    if (ensureLogin()) {
                        FunctionActivity.showFragment(getActivity(), SubscribeFragment.class.getName(), getString(R.string.device_data));
                    }
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private boolean ensureLogin() {
            boolean login = Matrix.accountManager().isLogin();
            if (!login) {
                Toast.makeText(getActivity(), "Need login first", Toast.LENGTH_SHORT).show();
            }
            return login;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != RESULT_OK) {
                return;
            }
            if (requestCode == REQUEST_CODE_LOGIN) {
                String account = PreferencesUtils.getString(getActivity(), MATRIX_ACCOUNT);
                findPreference("sign_in").setTitle(getString(R.string.who_login, account));
            }
        }
    }

    public static class OthersFragment extends PreferenceFragment {
        public static final String TAG = "OthersFragment";

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
                case "cleanCache":
                    ((MainApplication) getActivity().getApplication()).cleanCache();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
