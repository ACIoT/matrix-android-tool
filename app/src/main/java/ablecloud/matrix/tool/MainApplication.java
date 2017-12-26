package ablecloud.matrix.tool;

import android.app.Application;
import android.content.SharedPreferences;

import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.service.Configuration;

/**
 * Created by wangkun on 02/08/2017.
 */

public class MainApplication extends Application {

    private static final String MAIN_DOMAIN = "mainDomain";
    private static final String MAIN_DOMAIN_ID = "mainDomainId";
    private static final String MODE = "mode";
    private static final String REGION = "region";
    private static final String ROUTER = "router";
    private static final String GATEWAY = "gateway";

    private static SharedPreferences preferences;

    private Configuration privateConfiguration = new Configuration() {
        @Override
        public String getRouterAddress() {
            return preferences.getString(ROUTER, null);
        }

        @Override
        public String getGatewayAddress() {
            return preferences.getString(GATEWAY, null);
        }

        @Override
        public String getDomainName() {
            return getMainDomain();
        }

        @Override
        public long getDomainId() {
            return getMainDomainId();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("ablecloud_tool", MODE_PRIVATE);
        if (isInited()) {
            if (preferences.contains(MODE) && preferences.contains(REGION)) {
                Matrix.init(this, getMainDomain(), getMainDomainId(),
                        preferences.getInt(MODE, Integer.MAX_VALUE), preferences.getInt(REGION, Integer.MAX_VALUE));
            } else {
                Matrix.init(this, privateConfiguration);
            }
        }
    }

    public static boolean isInited() {
        return preferences.contains(MAIN_DOMAIN)
                && preferences.contains(MAIN_DOMAIN_ID);
    }

    public static String getMainDomain() {
        return preferences.getString(MAIN_DOMAIN, null);
    }

    public static long getMainDomainId() {
        return preferences.getLong(MAIN_DOMAIN_ID, 0);
    }

    public void init(String mainDomain, Long mainDomainId, int mode, int region) {
        preferences.edit().putString(MAIN_DOMAIN, mainDomain)
                .putLong(MAIN_DOMAIN_ID, mainDomainId)
                .putInt(MODE, mode)
                .putInt(REGION, region)
                .apply();
        Matrix.init(this, mainDomain, mainDomainId, mode, region);
    }

    public void init(final String mainDomain, final Long mainDomainId, final String router, final String gateway) {
        preferences.edit().putString(MAIN_DOMAIN, mainDomain)
                .putLong(MAIN_DOMAIN_ID, mainDomainId)
                .putString(ROUTER, router)
                .putString(GATEWAY, gateway)
                .apply();
        Matrix.init(this, privateConfiguration);
    }

    public void cleanCache() {
        preferences
                .edit()
                .clear()
                .commit();
    }
}
