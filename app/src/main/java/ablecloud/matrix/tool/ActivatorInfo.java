package ablecloud.matrix.tool;

import ablecloud.matrix.activator.DeviceActivator;

/**
 * Created by wangkun on 17/08/2017.
 */

public enum ActivatorInfo {
    HF(DeviceActivator.HF),
    MX(DeviceActivator.MX),
    QCSNIFFER(DeviceActivator.QCSNIFFER),
    ESP8266(DeviceActivator.ESP8266),
    REALTEK(DeviceActivator.REALTEK),
    AI6060H(DeviceActivator.AI6060H),
    BL(DeviceActivator.BL),
    QCLTLINK(DeviceActivator.QCLTLINK);

    public final int type;

    ActivatorInfo(int type) {
        this.type = type;
    }
}
