package ablecloud.matrix.helper;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.model.Device;

/**
 * Created by wangkun on 04/08/2017.
 */

public class DeviceManager {
    private static final String TAG = "DeviceManager";

    private static DeviceManager sInstance;

    private List<Device> devices = new ArrayList<>();
    private DataSetObservable dataSetObservable = new DataSetObservable();

    private DeviceManager() {
    }

    private static DeviceManager instance() {
        if (sInstance == null) {
            sInstance = new DeviceManager();
        }
        return sInstance;
    }

    public static List<Device> getDevices() {
        return instance().devices;
    }

    public static void updateDevices() {
        Matrix.bindManager().listDevices(new MatrixCallback<List<Device>>() {
            @Override
            public void success(List<Device> devices) {
                instance().devices.clear();
                instance().devices.addAll(devices);
                instance().dataSetObservable.notifyChanged();
            }

            @Override
            public void error(MatrixError matrixError) {
                Log.d(TAG, "listDevices error: " + matrixError.getMessage());
            }
        });
    }

    public static void registerDataSetObserver(DataSetObserver observer) {
        instance().dataSetObservable.registerObserver(observer);
    }

    public static void unregisterDataSetObserver(DataSetObserver observer) {
        instance().dataSetObservable.unregisterObserver(observer);
    }
}
