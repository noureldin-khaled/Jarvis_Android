package com.iot.guc.jarvis;

/**
 * Created by MariamMazen on 2017-04-02.
 */

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.models.Params;
import com.iot.guc.jarvis.requests.CustomJsonRequest;
import com.iot.guc.jarvis.responses.ChatResponse;
import com.iot.guc.jarvis.responses.HTTPResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class ChatAPI {

    public static AIService aiService;
    public static AIDataService aiDataService;
    public static RequestQueue queue;
    static boolean incompleteLight = false;
    static String incompleteLightMessage = "";
    static String countryCode = "";
    static String weatherForeCast = "";
    static String message;
    static boolean status;
    static Device d;

    public static void handleChat(String requestMessage, Context context, final ChatResponse chatResponse) {

        status = false;
        message = "";
        d = null;
        ArrayList<Room> rooms = Shared.getRooms();


        try {
            AIRequest aiRequest = new AIRequest();
            if(!incompleteLightMessage.isEmpty()){
                aiRequest.setQuery(incompleteLightMessage+requestMessage);
                incompleteLightMessage = "";
                Log.i("QUERY",incompleteLightMessage);
            }
            else{
                aiRequest.setQuery(requestMessage);
            }

            aiService.textRequest(aiRequest);
            AIResponse aiResponse = aiDataService.request(aiRequest);

            String action = aiResponse.getResult().getAction();

            String roomName = "";
            Log.i("Action", aiResponse.getResult().getParameters().toString());

            if (action.startsWith("smarthome")) {
                if (action.equals("smarthome.lights_on")) {
                    if (aiResponse.getResult().isActionIncomplete()) {
                        message = aiResponse.getResult().getFulfillment().getSpeech();
                        if (incompleteLight) {

                            incompleteLight = false;
                        } else {
                            incompleteLightMessage = "turn on the light in the ";
                            incompleteLight = true;
                        }

                    } else {
                        roomName = aiResponse.getResult().getStringParameter("Location");
                        status = true;
                    }
                } else if (action.equals("smarthome.lights_off")) {
                    if (aiResponse.getResult().isActionIncomplete()) {
                        message = aiResponse.getResult().getFulfillment().getSpeech();
                        if (incompleteLight) {

                            incompleteLight = false;
                        } else {
                            incompleteLightMessage = "turn off the light in the ";
                            incompleteLight = true;
                        }
                    } else {
                        roomName = aiResponse.getResult().getStringParameter("Location");
                        status = false;
                    }
                }

                if (!roomName.isEmpty()) {
                    for (Room room : rooms) {
                        if (room.getName().equalsIgnoreCase(roomName)) {
                            for (Device device : Shared.getDevices()) {
                                if (device.getType() == Device.TYPE.LIGHT_BULB && device.getRoom_id() == room.getId()) {
                                    d = device;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    if (d == null) {
                        message = "Device does not exist in the room";
                    }
                }
                chatResponse.onSuccess(200,new Params(d,status,message));
            }
            else if(action.equals("weatherForeCast")){


                String cityName = aiResponse.getResult().getStringParameter("geo-city");
                //here
                final String URL = Shared.getServer().URL() + "/api/country/";
                JSONObject body = new JSONObject();
                try {
                    body.put("cityName", cityName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    JsonObjectRequest cityCountryrequest = new JsonObjectRequest(Request.Method.POST, URL, body, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray countries = response.getJSONArray("code");
                                if(countries.length() > 0){
                                    countryCode = countries.getJSONObject(0).getString("country");
                                    String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q="+countries.getJSONObject(0).getString("name").toLowerCase()+","+countryCode+"&id=524901&APPID=88704427a46ca5ed706fdcd943b95cb9";
                                    try {
                                        final JsonObjectRequest weatherForeCastrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    message = response.getJSONArray("list").getJSONObject(0).getJSONObject("temp").getDouble("day")+"";
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                chatResponse.onSuccess(200,new Params(d,status,message));
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                message = "Something went wrong here";
                                                chatResponse.onSuccess(200,new Params(d,status,message));
                                            }
                                        }) {
                                            @Override
                                            public Map<String, String> getHeaders() throws AuthFailureError {
                                                Map<String, String> headers = new HashMap<>();
                                                headers.put("Content-Type", "application/json");
                                                return headers;
                                            }
                                        };
                                        queue.add(weatherForeCastrequest);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    message = "Please enter a valid city name";
                                    chatResponse.onSuccess(200,new Params(d,status,message));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("error",error.toString());
                            message = "Something went wrong here again";
                            chatResponse.onSuccess(200,new Params(d,status,message));
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Authorization", Shared.getAuth().getToken());
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }
                    };
                    queue.add(cityCountryrequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //here

            }
            else {
                message = aiResponse.getResult().getFulfillment().getSpeech();
                chatResponse.onSuccess(200,new Params(d,status,message));
            }

        } catch (AIServiceException e) {
            message = "Something Went Wrong!";
            chatResponse.onSuccess(200,new Params(d,status,message));
            e.printStackTrace();
        }

    }



//    public static void country(Context context, String cityName, final HTTPResponse httpResponse){
//
//        RequestQueue queue = Volley.newRequestQueue(context);
//        final String URL = Shared.getServer().URL() + "/api/country/";
//        JSONObject body = new JSONObject();
//        try {
//            body.put("cityName", cityName);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            JsonObjectRequest cityCountryrequest = new JsonObjectRequest(Request.Method.POST, URL, body, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    httpResponse.onSuccess(200,response);
//                    try {
//                        JSONArray countries = response.getJSONArray("code");
//                        if(countries.length() > 0){
//                            countryCode = countries.getJSONObject(0).getString("country");
//                            String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q="+countries.getJSONObject(0).getString("name").toLowerCase()+","+countryCode+"&id=524901&APPID=88704427a46ca5ed706fdcd943b95cb9";
//                            weatherForeCast(context,url);
//                            Log.d("cooode", countryCode);
//                        }
//                        else{
//                            Log.d("cde", countryCode);
//                            countryCode = "";
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("resp", response.toString());
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d("error",error.toString());
//                }
//            }) {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    Map<String, String> headers = new HashMap<>();
//                    headers.put("Authorization", Shared.getAuth().getToken());
//                    headers.put("Content-Type", "application/json");
//                    return headers;
//                }
//            };
//            queue.add(cityCountryrequest);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//

//    public static void weatherForeCast(Context context, String url){
//
//        RequestQueue queue = Volley.newRequestQueue(context);
//
//        try {
//            final JsonObjectRequest weatherForeCastrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    weatherForeCast = response.toString();
//                    Log.d("weather forecast", response.toString());
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d("error",error.toString());
//                }
//            }) {
//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    Map<String, String> headers = new HashMap<>();
//                    headers.put("Content-Type", "application/json");
//                    return headers;
//                }
//            };
//            queue.add(weatherForeCastrequest);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
}
