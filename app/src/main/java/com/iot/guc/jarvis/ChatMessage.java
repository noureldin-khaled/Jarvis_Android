package com.iot.guc.jarvis;
import java.util.Random;

public class ChatMessage {

    public String body;
    public String Date, Time;
    public boolean isMine;

    public ChatMessage(String messageString, boolean isMINE, String date, String time) {
        body = messageString;
        isMine = isMINE;
        Date = date;
        Time = time;
    }
}