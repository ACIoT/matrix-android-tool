package ablecloud.matrix.tool;

import android.app.Application;

import ablecloud.matrix.app.Matrix;

/**
 * Created by wangkun on 02/08/2017.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Matrix.init(this, BuildConfig.MAJOR_DOMAIN, BuildConfig.MAJOR_DOMAIN_ID, Matrix.TEST_MODE);
    }
}
