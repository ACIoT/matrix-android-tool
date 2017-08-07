package ablecloud.matrix.tool;

import android.app.Application;
import android.content.SharedPreferences;

import ablecloud.matrix.app.Matrix;

/**
 * Created by wangkun on 02/08/2017.
 */

public class MainApplication extends Application {

    private static final String MAJOR_DOMAIN = "majorDomain";
    private static final String MAJOR_DOMAIN_ID = "majorDomainId";

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences("ablecloud_tool", MODE_PRIVATE);
        if (!preferences.contains(MAJOR_DOMAIN) || !preferences.contains(MAJOR_DOMAIN_ID)) {
            preferences.edit()
                    .putString(MAJOR_DOMAIN, BuildConfig.MAJOR_DOMAIN)
                    .putLong(MAJOR_DOMAIN_ID, BuildConfig.MAJOR_DOMAIN_ID).apply();
        }
        Matrix.init(this, preferences.getString(MAJOR_DOMAIN, null), preferences.getLong(MAJOR_DOMAIN_ID, 0), Matrix.TEST_MODE);
    }
}
