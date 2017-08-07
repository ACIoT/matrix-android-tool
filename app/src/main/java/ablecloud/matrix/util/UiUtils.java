package ablecloud.matrix.util;

import android.content.Context;
import android.widget.Toast;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

/**
 * Created by wangkun on 07/08/2017.
 */

public class UiUtils {
    public static void toast(final Context context, final String message) {
        Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void runOnUiThread(Action action) {
        Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe(action);
    }
}
