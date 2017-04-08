package com.iot.guc.jarvis.models;

public class Event {

    private String time;
    private String device;
    private int device_id;
    private Boolean status;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public int getDevice_id() {
        return device_id;
    }

    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }

}
