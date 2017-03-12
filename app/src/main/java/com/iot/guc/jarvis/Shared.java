package com.iot.guc.jarvis;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Shared {
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");
    private static Server server = new Server("192.168.1.206", 8000);
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

    public static void clearRooms() {
        rooms.clear();
    }

    public static void addDevice(Device d) {
        devices.add(d);
    }

    public static void removeDevice(int index) {
        devices.remove(index);
    }

    public static void clearDevices() {
        devices.clear();
    }


    //API REQUESTS

    public static void deleteDevice(int device_id){
        Log.e("SHARED","Device No."+ device_id);
    }

    public static void turnOnDevice(int id) {
        Log.e("SHARED","Handle Device No."+ id);
    }

    public static void turnOffDevice(int id) {
        Log.e("SHARED","Handle Device No."+ id);
    }

    public static  void addRoom(String name){
        Log.e("SHARED","Adding "+ name);
    }

    public static  void addDevice(int roomIndex, String name){
        Log.e("SHARED","Adding "+name+" into room "+roomIndex);

    }
}