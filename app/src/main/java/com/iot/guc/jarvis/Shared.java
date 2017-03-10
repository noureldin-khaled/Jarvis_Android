package com.iot.guc.jarvis;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Shared {
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");
    private static Server server = new Server("192.168.1.3", 8000);
    private static User auth;
    private static ArrayList<Room> rooms = new ArrayList<>();
    private static ArrayList<Device> devices = new ArrayList<>();

    public static String getCurrentTime() {
        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate() {
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    public static Server getServer() {
        return server;
    }

    public static void setServer(Server server) {
        Shared.server = server;
    }

    public static User getAuth() {
        return auth;
    }

    public static void setAuth(User auth) {
        Shared.auth = auth;
    }

    public static ArrayList<Room> getRooms() {
        return rooms;
    }

    public static ArrayList<Device> getDevices() {
        return devices;
    }

    public static void addRoom(Room r) {
        rooms.add(r);
    }

    public static void removeRoom(int index) {
        rooms.remove(index);
    }

    public static void addDevice(Device d) {
        devices.add(d);
    }

    public static void removeDevice(int index) {
        devices.remove(index);
    }
}