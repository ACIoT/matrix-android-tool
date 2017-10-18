package ablecloud.matrix.tool;

import android.app.Application;
import android.content.SharedPreferences;

import ablecloud.matrix.app.Matrix;

/**
 * Created by wangkun on 02/08/2017.
 */

public class MainApplication extends Application {

    private static final String MAIN_DOMAIN = "mainDomain";
    private static final String MAIN_DOMAIN_ID = "mainDomainId";
    private static final String MODE = "mode";
    private static final String REGION = "region";
    private static SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("ablecloud_tool", MODE_PRIVATE);
        if (isInited()) {
            Matrix.init(this, getMainDomain(), getMainDomainId(), getMode(), getRegion());
        }
    }

    public static boolean isInited() {
        return preferences.contains(MAIN_DOMAIN)
                && preferences.contains(MAIN_DOMAIN_ID)
                && preferences.contains(MODE)
                && preferences.contains(REGION);
    }
    public static String getMainDomain() {
        return preferences.getString(MAIN_DOMAIN, null);
    }

    public static long getMainDomainId() {
        return preferences.getLong(MAIN_DOMAIN_ID, 0);
    }

    public static int getMode() {
        return preferences.getInt(MODE, Integer.MAX_VALUE);
    }

    public static int getRegion() {
        return preferences.getInt(REGION, Integer.MAX_VALUE);
    }

    public void init(String mainDomain, Long mainDomainId, int mode, int region) {
        preferences.edit().putString(MAIN_DOMAIN, mainDomain)
                .putLong(MAIN_DOMAIN_ID, mainDomainId)
                .putInt(MODE, mode)
                .putInt(REGION, region)
                .apply();
        Matrix.init(this, mainDomain, mainDomainId, mode, region);
    }
}
