package com.iot.guc.jarvis;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username, token, type;

    public User(int id, String username, String token, String type) {
        this.id = id;
        this.username = username;
        this.token = token;
        this.type = type;
    }
}
