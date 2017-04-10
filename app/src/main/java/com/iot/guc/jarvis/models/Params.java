package com.iot.guc.jarvis.models;

/**
 * Created by MariamMazen on 2017-04-02.
 */

public class Params {
    Device device;
    boolean status;
    String message;

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Params(Device device, boolean status, String message) {
        this.device = device;
        this.status = status;
        this.message = message;
    }
}
