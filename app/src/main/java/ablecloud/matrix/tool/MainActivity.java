package ablecloud.matrix.tool;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.User;
import ablecloud.matrix.util.PreferencesUtils;
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
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        boolean login = Matrix.accountManager().isLogin();
        if (!login) {
            menu.add(0, android.R.id.button1, 0, "登录").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add(0, android.R.id.button2, 0, PreferencesUtils.getString(this, MATRIX_ACCOUNT)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.button1:
                if (!loginDialog.isShowing())
                    loginDialog.show();
                return true;
            case android.R.id.button2:
                if (!logoutDialog.isShowing())
                    logoutDialog.show();
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
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            switch (preference.getKey()) {
                case "cloud_message":
                    FunctionActivity.showFragment(getActivity(), CloudMessageFragment.class.getName());
                    return true;
                case "ota_upgrade":
                    FunctionActivity.showFragment(getActivity(), OtaUpgradeFragment.class.getName());
                    return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
