package com.kriptops.wizarpos.cardlib.android;

import android.util.Log;

import com.cloudpos.Device;
import com.cloudpos.DeviceException;
import com.kriptops.wizarpos.cardlib.Defaults;

import java.io.Closeable;

public class AbstractCloseable<T extends Device> implements Closeable {

    private boolean open;
    protected final T device;

    protected AbstractCloseable (T device) {
        this.open = false;
        this.device = device;
    }

    public T getDevice () {
        return device;
    }

    public boolean isOpen(){
        return open;
    }

    public boolean open() {
        if (!open) {
            try {
                device.open();
                return (this.open = true);
            } catch (DeviceException e) {
                Log.d(Defaults.LOG_TAG, "No se puede abrir el dispositivo " + device.toString(), e);
            }
        }
        return false;
    }

    @Override
    public void close() {
        try {
            device.close();
        } catch (DeviceException e) {
            Log.d(Defaults.LOG_TAG, "no se puede cerrar", e);
        }
        this.open = false;
    }

}
