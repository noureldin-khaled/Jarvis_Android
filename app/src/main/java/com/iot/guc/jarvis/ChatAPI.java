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
import com.android.volley.toolbox.JsonObjectRequest;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.models.Params;
import com.iot.guc.jarvis.responses.ChatResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ai.api.AIServiceException;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import java.util.Date;

public class ChatAPI {

    public static AIService aiService;
    public static AIDataService aiDataService;
    public static RequestQueue queue;
    static boolean incompleteLight = false;
    static String incompleteLightMessage = "";
    static boolean incompleteWeather = false;
    static String incompleteWeatherMessage = "";
    static String countryCode = "";
    static String message;
    static boolean status;
    static Device d;

    public static String parseResponse(JSONObject response) throws JSONException {
        int count = response.getInt("cnt");
        JSONArray list = response.getJSONArray("list");
        String result = "";

        for(int i = 0; i < count; i++){
            JSONObject day = list.getJSONObject(i);
            long unixSeconds = day.getLong("dt");
            Date date = new Date(unixSeconds*1000L);
            result += date.toString().substring(0,10) + ", ";
            int temp = (int)Math.ceil(((day.getJSONObject("temp").getDouble("min") + day.getJSONObject("temp").getDouble("max")) / 2 ) - 273.15);
            result += temp + "°C, " + day.getJSONArray("weather").getJSONObject(0).getString("description") + "\n";
        }
        return result;
    }

    public static void handleChat(String requestMessage,final ChatResponse chatResponse) {

        status = false;
        message = "";
        d = null;
        ArrayList<Room> rooms = Shared.getRooms();


        try {
            AIRequest aiRequest = new AIRequest();
            if(!incompleteLightMessage.isEmpty()){
                aiRequest.setQuery(incompleteLightMessage+requestMessage);
                incompleteLightMessage = "";
                Log.d("QUERY",incompleteLightMessage);
            }
            else if(!incompleteWeatherMessage.isEmpty()){
                aiRequest.setQuery(incompleteWeatherMessage+requestMessage);
                incompleteWeatherMessage = "";
                Log.d("QUERY",incompleteWeatherMessage);
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
                chatResponse.onSuccess(new Params(d,status,message));
            }
            else if(action.equals("weatherForeCast")){
                String cityName = aiResponse.getResult().getStringParameter("geo-city");
                if (aiResponse.getResult().isActionIncomplete()) {
                    message = aiResponse.getResult().getFulfillment().getSpeech();
                    incompleteWeatherMessage = "weather in "+cityName+" ";
                    chatResponse.onSuccess(new Params(d,status,message));
                } else {

                    final int duration = Integer.parseInt(aiResponse.getResult().getStringParameter("duration"));
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
                                        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q="+countries.getJSONObject(0).getString("name").toLowerCase()+","+countryCode+"&cnt="+duration+"&id=524901&APPID=88704427a46ca5ed706fdcd943b95cb9";
                                        try {
                                            final JsonObjectRequest weatherForeCastrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {
                                                        message = parseResponse(response);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    chatResponse.onSuccess(new Params(d,status,message));
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    message = "Something went wrong here";
                                                    chatResponse.onSuccess(new Params(d,status,message));
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
                                        chatResponse.onSuccess(new Params(d,status,message));
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
                                chatResponse.onSuccess(new Params(d,status,message));
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

                }

            }
            else {
                message = aiResponse.getResult().getFulfillment().getSpeech();
                chatResponse.onSuccess(new Params(d,status,message));
            }

        } catch (AIServiceException e) {
            message = "Something Went Wrong!";
            chatResponse.onSuccess(new Params(d,status,message));
            e.printStackTrace();
        }

    }

}
