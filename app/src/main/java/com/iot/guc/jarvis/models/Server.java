package com.iot.guc.jarvis.models;

public class Server {
    private String IP;
    private int PORT;
    private String rsa_pu, aes_pu;

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

    public String getRsa_pu() {
        return rsa_pu;
    }

    public void setRsa_pu(String rsa_pu) {
        this.rsa_pu = rsa_pu;
    }

    public String getAes_pu() {
        return aes_pu;
    }

    public void setAes_pu(String aes_pu) {
        this.aes_pu = aes_pu;
    }
}
