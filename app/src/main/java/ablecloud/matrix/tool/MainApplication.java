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
    private static final String REDIRECT = "redirect";
    private static final String REGIONID = "regionId";

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
        public String getRedirectAddress() {
            return null;
        }

        @Override
        public String getRegionDes() {
            return null;
        }

        @Override
        public long getDomainId() {
            return getMainDomainId();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        preferences = getSharedPreferences("ablecloud_tool", MODE_PRIVATE);
        if (isInited()) {
            if (preferences.contains(REGIONID)) {
                Matrix.initI18N(this, getMainDomain()
                        , getMainDomainId()
                        , preferences.getString(ROUTER, null)
                        , preferences.getString(GATEWAY, null)
                        , preferences.getString(REDIRECT, null)
                        , preferences.getString(REGIONID, null));
            } else {
                if (preferences.contains(MODE) && preferences.contains(REGION)) {
                    Matrix.init(this, getMainDomain(), getMainDomainId(),
                            preferences.getInt(MODE, Integer.MAX_VALUE), preferences.getInt(REGION, Integer.MAX_VALUE));
                } else {
                    Matrix.init(this, privateConfiguration);
                }
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

    //公有云
    public void init(String mainDomain, Long mainDomainId, int mode, int region) {
        preferences.edit().putString(MAIN_DOMAIN, mainDomain)
                .putLong(MAIN_DOMAIN_ID, mainDomainId)
                .putInt(MODE, mode)
                .putInt(REGION, region)
                .apply();
        Matrix.init(this, mainDomain, mainDomainId, mode, region);
    }

    //私有云
    public void init(final String mainDomain, final Long mainDomainId, final String router, final String gateway) {
        preferences.edit().putString(MAIN_DOMAIN, mainDomain)
                .putLong(MAIN_DOMAIN_ID, mainDomainId)
                .putString(ROUTER, router)
                .putString(GATEWAY, gateway)
                .apply();
        Matrix.init(this, privateConfiguration);
    }

    //国际化
    public void initI18N(final String mainDomain, final Long mainDomainId, final String router, final String gateway, String redirect, String regionId) {

        preferences.edit().putString(MAIN_DOMAIN, mainDomain)
                .putLong(MAIN_DOMAIN_ID, mainDomainId)
                .putString(ROUTER, router)
                .putString(GATEWAY, gateway)
                .putString(REDIRECT, redirect)
                .putString(REGIONID, regionId)
                .apply();

        Matrix.initI18N(this, mainDomain, mainDomainId, router, gateway, redirect, regionId);

    }

    public void cleanCache() {
        Matrix.accountManager().logout();
        preferences
                .edit()
                .clear()
                .commit();
    }
}
