package com.iot.guc.jarvis.models;

public class Server {
    private String IP;
    private int PORT;

    public Server(String IP, int PORT) {
        this.IP = IP;
        this.PORT = PORT;
    }

    public String URL() {
        return "http://" + IP + ":" + PORT;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getPORT() {
        return PORT;
    }

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }
}
