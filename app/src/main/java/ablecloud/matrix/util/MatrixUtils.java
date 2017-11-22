package ablecloud.matrix.util;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by liuxiaofeng on 2017/11/22.
 */

public class MatrixUtils {
    public static void assertNotEmpty(Context context, String field, String value) {
        if (TextUtils.isEmpty(value)) {
            UiUtils.toast(context, field + "can not be empty");
        }
    }
}
