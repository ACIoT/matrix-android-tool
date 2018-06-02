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
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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
        Single.create(new SingleOnSubscribe<List<Device>>() {
            @Override
            public void subscribe(final SingleEmitter<List<Device>> e) throws Exception {
                Matrix.bindManager().listDevices(new MatrixCallback<List<Device>>() {
                    @Override
                    public void success(List<Device> devices) {
                        e.onSuccess(devices);
                    }

                    @Override
                    public void error(MatrixError matrixError) {
                        e.onError(matrixError);
                    }
                });
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Device>>() {
                    @Override
                    public void accept(List<Device> devices) throws Exception {
                        instance().devices.clear();
                        instance().devices.addAll(devices);
                        instance().dataSetObservable.notifyChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "listDevices error: " + throwable.getMessage());
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
