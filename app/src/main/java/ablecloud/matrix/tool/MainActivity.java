package ablecloud.matrix.tool;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sloydev.preferator.Preferator;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.User;
import ablecloud.matrix.util.PreferencesUtils;
import ablecloud.matrix.util.UiUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends ContainerActivity {

    private static final String MATRIX_ACCOUNT = "ablecloud_matrix_account";
    private AlertDialog loginDialog;
    private AlertDialog logoutDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addFragment(MainFragment.class);
        loginDialog = new AlertDialog.Builder(this)
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
                                invalidateOptionsMenu();
                            }
                        }).subscribe(new Consumer<User>() {
                            @Override
                            public void accept(@NonNull User user) throws Exception {
                                PreferencesUtils.putString(MainActivity.this, MATRIX_ACCOUNT, a);
                                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                Toast.makeText(MainActivity.this, "登录失败:" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        logoutDialog = new AlertDialog.Builder(this)
                .setMessage("确认退出？")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Matrix.accountManager().logout();
                        invalidateOptionsMenu();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem accountItem = menu.findItem(R.id.account);
        boolean login = Matrix.accountManager().isLogin();
        if (!login) {
            accountItem.setTitle(R.string.login);
        } else {
            accountItem.setTitle(PreferencesUtils.getString(this, MATRIX_ACCOUNT));
        }
        return true;
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

    public static class MainFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_main);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            ActionBar actionBar = UiUtils.getSupportActionBar(this);
            actionBar.setSubtitle(getString(R.string.main_domain) + ": " + MainApplication.getMainDomain() + ", " +
                    getString(R.string.main_domain_id) + ": " + MainApplication.getMainDomainId());
            return view;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()) {
                case "device_bind":
                    ensureLogin(new Action() {
                        @Override
                        public void run() throws Exception {
                            FunctionActivity.showFragment(getActivity(), DeviceBindFragment.class.getName());
                        }
                    });
                    return true;
                case "ota_upgrade":
                    ensureLogin(new Action() {
                        @Override
                        public void run() throws Exception {
                            FunctionActivity.showFragment(getActivity(), OtaUpgradeFragment.class.getName());
                        }
                    });
                    return true;
                case "cloud_message":
                    ensureLogin(new Action() {
                        @Override
                        public void run() throws Exception {
                            FunctionActivity.showFragment(getActivity(), CloudMessageFragment.class.getName());
                        }
                    });
                    return true;
                case "subscribe":
                    ensureLogin(new Action() {
                        @Override
                        public void run() throws Exception {
                            FunctionActivity.showFragment(getActivity(), SubscribeFragment.class.getName());
                        }
                    });
                    return true;
                case "ablelink":
                    FunctionActivity.showFragment(getActivity(), AblelinkFragment.class.getName());
                    return true;
                case "local_scan":
                    FunctionActivity.showFragment(getActivity(), LocalScanFragment.class.getName());
                    return true;
                case "settings":
                    ensureLogout(new Consumer<Boolean>() {
                        @Override
                        public void accept(@NonNull Boolean logout) throws Exception {
                            Preferator.launch(getActivity());
                        }
                    });
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void ensureLogin(Action complete) {
            Completable completable = Matrix.accountManager().isLogin() ? Completable.complete() : Completable.error(new IllegalStateException("Need login first"));
            completable.observeOn(AndroidSchedulers.mainThread()).subscribe(complete, new Consumer<Throwable>() {
                @Override
                public void accept(@NonNull Throwable throwable) throws Exception {
                    Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void ensureLogout(Consumer<Boolean> complete) {
            Observable
                    .create(new ObservableOnSubscribe<Boolean>() {
                        @Override
                        public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                            if (!Matrix.accountManager().isLogin()) {
                                emitter.onNext(true);
                            } else {
                                emitter.onError(new MatrixError(103, "Need logout first"));
                            }
                        }
                    })
                    .subscribe(complete, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
