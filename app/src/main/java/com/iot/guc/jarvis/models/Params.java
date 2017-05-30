package com.iot.guc.jarvis.models;

/**
 * Created by MariamMazen on 2017-04-02.
 */

public class Params {
    Device device;
    boolean status;
    String message;
    String songName;


    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

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

    public Params(Device device, boolean status, String message, String songName) {
        this.device = device;
        this.status = status;
        this.message = message;
        this.songName = songName;
    }
}
