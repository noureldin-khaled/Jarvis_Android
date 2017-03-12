package com.iot.guc.jarvis;

public class Device {
    private int id;
    private String name;
    private TYPE type;
    private boolean status;
    private String mac, ip;
    private int room_id;

    public Device(int id, String name, TYPE type, boolean status, String mac, String ip, int room_id) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.mac = mac;
        this.ip = ip;
        this.room_id = room_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getRoom_id() {
        return room_id;
    }

    public void setRoom_id(int room_id) {
        this.room_id = room_id;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", mac='" + mac + '\'' +
                ", ip='" + ip + '\'' +
                ", room_id=" + room_id +
                '}';
    }

    public enum TYPE {
        LIGHT_BULB, LOCK;

        @Override
        public String toString() {
            switch (this){
                case LIGHT_BULB: return "Light Bulb";
                case LOCK: return "Lock";
                default:return "";
            }
        }
    }
}
