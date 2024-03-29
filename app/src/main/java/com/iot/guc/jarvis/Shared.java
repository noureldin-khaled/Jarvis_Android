package com.iot.guc.jarvis;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.iot.guc.jarvis.fragments.ChatFragment;
import com.iot.guc.jarvis.fragments.DeviceFragment;
import com.iot.guc.jarvis.fragments.RoomFragment;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Event;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.models.Server;
import com.iot.guc.jarvis.models.User;
import com.iot.guc.jarvis.requests.CustomJsonRequest;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.responses.SecurityResponse;
import com.iot.guc.jarvis.responses.ServerResponse;
import com.iot.guc.jarvis.responses.StringResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Shared {
    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");
    private static Server server;
    private static User auth;
    private static ArrayList<Room> rooms = new ArrayList<>();
    private static ArrayList<Device> devices = new ArrayList<>();
    private static ArrayList<ArrayList<Event>> Patterns = new ArrayList<>();
    private static int selectedRoom = -1;
    private static String sharedKey;
    private static ArrayList<Event> serviceEvents;
    private static int serviceCount;
    private static ArrayList<Boolean> autoPattern;

    public static ArrayList<Boolean> getAutoPattern() {
        return autoPattern;
    }

    public static void setAutoPattern(ArrayList<Boolean> autoPattern) {
        Shared.autoPattern = autoPattern;
    }

    public static ArrayList<Event> getServiceEvents() {
        return serviceEvents;
    }

    public static void setServiceEvents(ArrayList<Event> serviceEvents) {
        Shared.serviceEvents = serviceEvents;
    }

    public static int getServiceCount() {
        return serviceCount;
    }

    public static void setServiceCount(int serviceCount) {
        Shared.serviceCount = serviceCount;
    }

    public static String getCurrentTime() {
        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate() {
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    public static ArrayList<ArrayList<Event>> getPatterns(){
        return Patterns;
    }

    public static void setPatterns(ArrayList<ArrayList<Event>> p){
        Shared.Patterns = p;
    }

    public static Server getServer() {
        return server;
    }

    public static String getSharedKey() {
        return sharedKey;
    }

    public static void setSharedKey(String sharedKey) {
        Shared.sharedKey = sharedKey;
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

    public static int getSelectedRoom() {
        return selectedRoom;
    }

    public static void setSelectedRoom(int selectedRoom) {
        Shared.selectedRoom = selectedRoom;
    }

    public static ArrayList<Room> getRooms() {
        return rooms;
    }

    public static ArrayList<Device> getDevices() {
        return devices;
    }

    public static ArrayList<Device> getDevices(int room_id) {
        ArrayList<Device> res = new ArrayList<>();
        for (Device d : devices)
            if (d.getRoom_id() == room_id)
                res.add(d);

        return res;
    }

    public static Device getDevice(int deviceId){
        for(int i=0; i <Shared.getDevices().size();i++){
            Device d = devices.get(i);
            if(d.getId()==deviceId)
                return d;
        }
        return null;
    }

    public static int deviceIndexOf(int deviceId) {
        for(int i=0; i <Shared.getDevices().size();i++){
            Device d = devices.get(i);
            if(d.getId()==deviceId)
                return i;
        }
        return -1;
    }

    public static void addRoomAPIAI(Room r, Context context){
        String entityUrl = "https://api.api.ai/v1/entities/9088204c-b4bf-4330-bb41-771b99af06ca/entries?v=20150910";
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        final String jsonString = "[\n" +
                " {\n" +
                "  \"value\": \""+r.getName()+"\",\n" +
                "  \"synonyms\": [\""+r.getName()+"\"]\n"+
                " }\n" +
                "]";
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            CustomJsonRequest jsonArrayRequest = new CustomJsonRequest(Request.Method.POST, entityUrl, jsonArray, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("resp", response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("error",error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer fe2437e5a86740a78ccdfac19d283494");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            requestQueue.add(jsonArrayRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void addRoom(Room r, Context context) {
        rooms.add(r);
        addRoomAPIAI(r,context);
    }

    public static void removeRoom(int index) {
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = devices.size() - 1; i >= 0; i--) {
            if (devices.get(i).getRoom_id() == rooms.get(index).getId())
                indices.add(i);
        }

        for (int i = 0; i < indices.size(); i++)
            removeDevice(indices.get(i));

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

    public static void removeDevice(Device d){
        int idx = -1;
        for(int i = 0; i < devices.size() && idx == -1; i++)
            if(d.getId() == devices.get(i).getId())
                idx = i;

        removeDevice(idx);
    }

    public static void clearDevices() {
        devices.clear();
    }

    public static void  editRoom(int index, Room room){
        rooms.set(index,room);
    }

    public static void editDevice(int index, Device device){
        devices.set(index, device);
    }

    public static String toString(JSONObject obj) throws JSONException {
        String s = "{";
        for(Iterator<String> iter = obj.keys(); iter.hasNext(); ) {
            String key = iter.next();
            Object value = obj.get(key);
            s += "\"" + key + "\":\"" + value + "\"";
            if (iter.hasNext())
                s += ",";
        }
        s += "}";
        return s;
    }

    public static void collapseKeyBoard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void collapseKeyBoard(RoomFragment fragment) {
        fragment.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void collapseKeyBoard(DeviceFragment fragment) {
        fragment.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void collapseKeyBoard(ChatFragment fragment) {
        fragment.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void RSAEncrypt(Context context, JSONObject body, final StringResponse stringResponse) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("text", body.toString());

            JSONcall(context, Request.Method.POST, "/rsaEncrypt", obj, new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    try {
                        stringResponse.onSuccess(200, body.getString("body"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        stringResponse.onFailure(500, null);
                    }
                }

                @Override
                public void onFailure(int statusCode, JSONObject body) {
                    stringResponse.onFailure(500, null);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            stringResponse.onFailure(500, null);
        }
    }

    public static void AESEncrypt(Context context, String body, final StringResponse stringResponse) {
        Stringcall(context, Request.Method.GET, "/AesEncrypt/" + sharedKey + "/" + body, stringResponse);
    }

    public static void AESDecrypt(Context context, String encrypted, final StringResponse stringResponse) {
        Stringcall(context, Request.Method.GET, "/AesDecrypt/" + sharedKey + "/" + encrypted, stringResponse);
    }

    public static void getNonce(Context context, final StringResponse stringResponse) {
        Stringcall(context, Request.Method.GET, "/nonce", stringResponse);
    }

    public static void JSONcall(Context context, int method, String url, JSONObject body, final HTTPResponse httpResponse) {
        RequestQueue queue = Volley.newRequestQueue(context);
        url = "https://mighty-savannah-17728.herokuapp.com" + url;
        JsonObjectRequest request = new JsonObjectRequest(method, url, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                httpResponse.onSuccess(200, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                httpResponse.onFailure(500, null);
            }
        });

        queue.add(request);
    }

    public static void Stringcall(Context context, int method, String url, final StringResponse stringResponse) {
        RequestQueue queue = Volley.newRequestQueue(context);
        url = "https://mighty-savannah-17728.herokuapp.com" + url;
        StringRequest request = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                stringResponse.onSuccess(200, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                stringResponse.onFailure(500, null);
            }
        });

        queue.add(request);
    }

    public static void makeRequest(final Context context, final int method, final String url, final JSONObject body, final int headers, final String username, final boolean decrypt, final HTTPResponse httpResponse) {
        new ServerTask(new ServerResponse() {
            @Override
            public void onFinish() {
                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest request;
                final String URL = server.URL() + url;
                if (headers == Constants.AUTH_HEADERS) {
                    request = new JsonObjectRequest(method, URL, body, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (decrypt) {
                                try {
                                    AESDecrypt(context, response.getString("body"), new StringResponse() {
                                        @Override
                                        public void onSuccess(int statusCode, String response) {
                                            try {
                                                httpResponse.onSuccess(200, new JSONObject(response));
                                            } catch (JSONException e) {
                                                // The app failed
                                                httpResponse.onFailure(Constants.APP_FAILURE, null);
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(int statusCode, String response) {
                                            httpResponse.onFailure(500, null);
                                        }
                                    });
                                } catch (JSONException e) {
                                    // The app failed
                                    httpResponse.onFailure(Constants.APP_FAILURE, null);
                                    e.printStackTrace();
                                }
                            }
                            else
                                httpResponse.onSuccess(200, response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse == null) {
                                // The server couldn't be reached
                                server = null;
                                httpResponse.onFailure(Constants.SERVER_NOT_REACHED, null);
                            }
                            else {
                                try {
                                    String err = new String(error.networkResponse.data, "UTF-8");
                                    JSONObject json = new JSONObject(err);
                                    httpResponse.onFailure(error.networkResponse.statusCode, json);
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    // The app failed
                                    httpResponse.onFailure(Constants.APP_FAILURE, null);
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Authorization", auth.getToken());
                            return headers;
                        }
                    };
                }
                else if (headers == Constants.USERNAME_HEADERS) {
                    request = new JsonObjectRequest(method, URL, body, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (decrypt) {
                                try {
                                    AESDecrypt(context, response.getString("body"), new StringResponse() {
                                        @Override
                                        public void onSuccess(int statusCode, String response) {
                                            try {
                                                httpResponse.onSuccess(200, new JSONObject(response));
                                            } catch (JSONException e) {
                                                // The app failed
                                                httpResponse.onFailure(Constants.APP_FAILURE, null);
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(int statusCode, String response) {
                                            httpResponse.onFailure(500, null);
                                        }
                                    });
                                } catch (JSONException e) {
                                    // The app failed
                                    httpResponse.onFailure(Constants.APP_FAILURE, null);
                                    e.printStackTrace();
                                }
                            }
                            else
                                httpResponse.onSuccess(200, response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse == null) {
                                // The server couldn't be reached
                                server = null;
                                httpResponse.onFailure(Constants.SERVER_NOT_REACHED, null);
                            }
                            else {
                                try {
                                    String err = new String(error.networkResponse.data, "UTF-8");
                                    JSONObject json = new JSONObject(err);
                                    httpResponse.onFailure(error.networkResponse.statusCode, json);
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    // The app failed
                                    httpResponse.onFailure(Constants.APP_FAILURE, null);
                                    e.printStackTrace();
                                }
                            }
                        }
                    })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("username", username);
                            return headers;
                        }
                    };
                }
                else {
                    request = new JsonObjectRequest(method, URL, body, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (decrypt) {
                                try {
                                    AESDecrypt(context, response.getString("body"), new StringResponse() {
                                        @Override
                                        public void onSuccess(int statusCode, String response) {
                                            try {
                                                httpResponse.onSuccess(200, new JSONObject(response));
                                            } catch (JSONException e) {
                                                // The app failed
                                                httpResponse.onFailure(Constants.APP_FAILURE, null);
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(int statusCode, String response) {
                                            httpResponse.onFailure(500, null);
                                        }
                                    });
                                } catch (JSONException e) {
                                    // The app failed
                                    httpResponse.onFailure(Constants.APP_FAILURE, null);
                                    e.printStackTrace();
                                }
                            }
                            else
                                httpResponse.onSuccess(200, response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse == null) {
                                // The server couldn't be reached
                                httpResponse.onFailure(Constants.SERVER_NOT_REACHED, null);
                            }
                            else {
                                try {
                                    String err = new String(error.networkResponse.data, "UTF-8");
                                    JSONObject json = new JSONObject(err);
                                    httpResponse.onFailure(error.networkResponse.statusCode, json);
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    // The app failed
                                    httpResponse.onFailure(Constants.APP_FAILURE, null);
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }

                request.setRetryPolicy(new DefaultRetryPolicy(600000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            }
        }).start();
    }

    public static void processEncryption(final Context context, final int method, final String url, final JSONObject body, final int headers, final String username, int encryption, final boolean decrypt, final HTTPResponse httpResponse) {
        if (encryption == Constants.RSA_ENCRYPTION) {
            RSAEncrypt(context, body, new StringResponse() {
                @Override
                public void onSuccess(int statusCode, String response) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("body", response);
                        makeRequest(context, method, url, obj, headers, username, decrypt, httpResponse);
                    } catch (JSONException e) {
                        // The app failed
                        httpResponse.onFailure(Constants.APP_FAILURE, null);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, String response) {
                    httpResponse.onFailure(500, null);
                }
            });
        }
        else if (encryption == Constants.AES_ENCRYPTION) {
            AESEncrypt(context, body.toString(), new StringResponse() {
                @Override
                public void onSuccess(int statusCode, String response) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("body", response);
                        makeRequest(context, method, url, obj, headers, username, decrypt, httpResponse);
                    } catch (JSONException e) {
                        // The app failed
                        httpResponse.onFailure(Constants.APP_FAILURE, null);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, String response) {
                    httpResponse.onFailure(500, null);
                }
            });
        }
        else {
            makeRequest(context, method, url, body, headers, username, decrypt, httpResponse);
        }
    }

    public static void request(final Context context, final int method, final String url, final JSONObject body, final int headers, final String username, final int encryption, final boolean decrypt, final boolean attachNonce, final HTTPResponse httpResponse) {
        Log.i("here", "request: " + url);
        if (attachNonce) {
            getNonce(context, new StringResponse() {
                @Override
                public void onSuccess(int statusCode, String response) {
                    try {
                        body.put("nonce", response);
                        processEncryption(context, method, url, body, headers, username, encryption, decrypt, httpResponse);
                    } catch (JSONException e) {
                        // The app failed
                        httpResponse.onFailure(Constants.APP_FAILURE, null);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, String response) {
                    httpResponse.onFailure(500, null);
                }
            });
        }
        else
            processEncryption(context, method, url, body, headers, username, encryption, decrypt, httpResponse);
    }

    public static int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
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