package com.iot.guc.jarvis;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Common {
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");
    private static String IP = "192.168.1.9";
    private static String PORT = "8000";

    public static String getCurrentTime() {
        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate() {
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    public static String getIP() {
        return IP;
    }

    public static void setIP(String IP) {
        Common.IP = IP;
    }

    public static String getPORT() {
        return PORT;
    }

    public static void setPORT(String PORT) {
        Common.PORT = PORT;
    }
}