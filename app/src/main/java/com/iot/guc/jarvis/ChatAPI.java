package com.iot.guc.jarvis;

/**
 * Created by MariamMazen on 2017-04-02.
 */

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
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
import com.iot.guc.jarvis.responses.HTTPResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import ai.api.AIServiceException;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

public class ChatAPI extends AppCompatActivity {

    public static AIService aiService;
    public static AIDataService aiDataService;
    public static RequestQueue queue;
    static boolean incompleteLight = false;
    static String incompleteLightMessage = "";
    static String incompleteWeatherMessage = "";
    static String message;
    static boolean status;
    static Device d;
    static Context context;
    static Activity activity;
    static ContentValues eventValues;
    static String eventUriString = "content://com.android.calendar/events";
    static String reminderUriString = "content://com.android.calendar/reminders";
    static ChatResponse response;
    static String appointmentDescription = "";
    static String appointmentLocation = "";
    static String needReminder = "";
    static Calendar cal = Calendar.getInstance();
    static long eventID;
    static int duration;
    static String cityName;
    static boolean chosenCountry = false;
    static boolean incompleteMusic = false;

    public static String parseResponse(JSONObject response) throws JSONException {
        int count = response.getInt("cnt");
        JSONArray list = response.getJSONArray("list");
        String result = "";

        for (int i = 0; i < count; i++) {
            JSONObject day = list.getJSONObject(i);
            long unixSeconds = day.getLong("dt");
            Date date = new Date(unixSeconds * 1000L);
            result += date.toString().substring(0, 10) + ", ";
            int temp = (int) Math.ceil(((day.getJSONObject("temp").getDouble("min") + day.getJSONObject("temp").getDouble("max")) / 2) - 273.15);
            result += temp + "°C, " + day.getJSONArray("weather").getJSONObject(0).getString("description");
            if(i < count-1)
                result += "\n";
        }
        return result;
    }

    public static String getCountriesList(JSONArray countries) throws IOException, JSONException {
        String result = "";
        HashSet<String> countryCodes = new HashSet<String>();
        for(int i = 0; i < countries.length(); i++){
            String code = countries.getJSONObject(i).getString("country");
            if(!countryCodes.contains(code)){
                countryCodes.add(code);
                result += countries.getJSONObject(i).getString("country");
                if(i < countries.length()-1)
                    result += "\n";
            }

        }
        return result;
    }

    public static void handleChat(String requestMessage, Context applicationContext, Activity callingActivity, final ChatResponse chatResponse) {

        status = false;
        message = "";
        d = null;
        ArrayList<Room> rooms = Shared.getRooms();
        context = applicationContext;
        activity = callingActivity;
        response = chatResponse;


        try {
            AIRequest aiRequest = new AIRequest();
            if (!incompleteLightMessage.isEmpty()) {
                aiRequest.setQuery(incompleteLightMessage + requestMessage);
                incompleteLightMessage = "";
                incompleteLight = false;

            } else if (!incompleteWeatherMessage.isEmpty()) {
                aiRequest.setQuery(incompleteWeatherMessage + requestMessage);
                incompleteWeatherMessage = "";
            }
            else if(incompleteMusic){
                incompleteMusic = false;
                aiRequest.setQuery("play " + requestMessage);
            }
            else {
                aiRequest.setQuery(requestMessage);
            }

            aiService.textRequest(aiRequest);
            AIResponse aiResponse = aiDataService.request(aiRequest);

            String action = aiResponse.getResult().getAction();
            Log.d("Action",action);

            String roomName = "";

            if(chosenCountry){
                chosenCountry = false;
                weatherForecast(requestMessage.toUpperCase());
            }
            else if(!appointmentDescription.isEmpty() && appointmentLocation.isEmpty() && needReminder.isEmpty()){
                appointmentDescription = requestMessage;
                appointmentLocation = "location";
                chatResponse.onSuccess(new Params(d, status, "Well, what about the location ?",""));
            }
            else if(!appointmentDescription.isEmpty() && !appointmentLocation.isEmpty() && needReminder.isEmpty()) {
                appointmentLocation = requestMessage;
                needReminder = "no";
                chatResponse.onSuccess(new Params(d, status, "Well, do u want me to set any reminders ?",""));
            }
            else if(!appointmentDescription.isEmpty() && !appointmentLocation.isEmpty() && !needReminder.isEmpty()){
                needReminder = requestMessage;

                eventValues = new ContentValues();
                eventValues.put("calendar_id", 1);
                eventValues.put("title", appointmentLocation+"'s appointment");
                eventValues.put("description", appointmentDescription);
                eventValues.put("eventLocation", appointmentLocation);

                long endDate = cal.getTimeInMillis() + 1000 * 60 * 60;

                eventValues.put("dtstart", cal.getTimeInMillis());
                eventValues.put("dtend", endDate);
                eventValues.put("eventTimezone", "UTC/GMT +2:00");

                eventValues.put("hasAlarm", 1);

                appointmentDescription = "";
                appointmentLocation = "";

                Uri eventUri;
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR}, 4);
                else {
                    eventUri = activity.getApplicationContext().getContentResolver().insert(Uri.parse(eventUriString), eventValues);
                    eventID = Long.parseLong(eventUri.getLastPathSegment());
                    if(needReminder.toLowerCase().startsWith("y")){
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put("event_id", eventID);
                        reminderValues.put("minutes", 5);
                        reminderValues.put("method", 1);
                        Uri reminderUri = activity.getApplicationContext().getContentResolver().insert(Uri.parse(reminderUriString), reminderValues);
                    }
                    needReminder = "";
                    chatResponse.onSuccess(new Params(d, status, "Appointment set !",""));
                }
            }
            else if(action.equals("music")){
                if(aiResponse.getResult().getStringParameter("song-name").isEmpty()){
                    incompleteMusic = true;
                    chatResponse.onSuccess(new Params(d, status, "Sure, what song do you want to listen to ?",""));
                }
                else{
                    chatResponse.onSuccess(new Params(d, status, message,aiResponse.getResult().getStringParameter("song-name")));
                }
            }
            else if (action.startsWith("smarthome")) {
                if (action.equals("smarthome.lights_on")) {
                    if (aiResponse.getResult().isActionIncomplete()) {
                        message = aiResponse.getResult().getFulfillment().getSpeech();
                        incompleteLightMessage = "turn on the light in the ";
                        incompleteLight = true;
                    } else {
                        roomName = aiResponse.getResult().getStringParameter("Location");
                        status = true;
                    }
                } else if (action.equals("smarthome.lights_off")) {
                    if (aiResponse.getResult().isActionIncomplete()) {
                        message = aiResponse.getResult().getFulfillment().getSpeech();
                        incompleteLightMessage = "turn off the light in the ";
                        incompleteLight = true;
                        }
                    else {
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
                chatResponse.onSuccess(new Params(d, status, message,""));
            } else if (action.equals("weatherForeCast")) {
                cityName = aiResponse.getResult().getStringParameter("geo-city");
                if (aiResponse.getResult().isActionIncomplete()) {
                    if(aiResponse.getResult().getFulfillment().getSpeech().equals("What is the geo-city?")){
                        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                        intent.putExtra(SearchManager.QUERY, requestMessage);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                    else{
                        message = aiResponse.getResult().getFulfillment().getSpeech();
                        incompleteWeatherMessage = "weather in " + cityName + " ";
                        chatResponse.onSuccess(new Params(d, status, message,""));
                    }

                } else {
                    duration = Integer.parseInt(aiResponse.getResult().getStringParameter("duration"));
                    handleWeather(context, cityName, new HTTPResponse() {
                        @Override
                        public void onSuccess(int statusCode, JSONObject body) {
                            try {
                                Log.d("BODYYY",body.toString());
                                JSONArray countries = body.getJSONArray("code");
                                if(countries.length() > 1){
                                    try {
                                        chosenCountry = true;
                                        message = "Please choose one of the following country codes \n" + getCountriesList(countries);
                                        chatResponse.onSuccess(new Params(d,status,message,""));
                                    } catch (IOException e) {
                                        chatResponse.onSuccess(new Params(d,status,"Something went wrong",""));
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    weatherForecast(countries.getJSONObject(0).getString("country"));
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, JSONObject body) {
                            chatResponse.onSuccess(new Params(d,status,"Something went wrong",""));

                        }
                    });
                }

            } else if(action.equals("appointment")){
                String dateTime = aiResponse.getResult().getStringParameter("date-time");

                if(dateTime.length() > 8){

                    String[] dateArray = dateTime.substring(0,10).split("-");
                    String[] date_time = dateTime.split("T");

                    int year = Integer.parseInt(dateArray[0]);
                    int month = Integer.parseInt(dateArray[1]);
                    int date = Integer.parseInt(dateArray[2]);

                    Date appointmentDate = cal.getTime();

                    final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                    String time = date_time[1].substring(0,5);
                    try{
                        appointmentDate = simpleDateFormat.parse(time);
                    }
                    catch(ParseException e){
                        e.printStackTrace();
                    }

                    cal.setTime(appointmentDate);

                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.DAY_OF_MONTH,date);
                    cal.set(Calendar.MONTH, month-1);

                }
                else if(dateTime.length() == 8){

                    String[] timeArray = dateTime.split(":");
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]));
                    cal.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]));
                    cal.set(Calendar.SECOND, 0);

                }
                else{
                    // default appointment at 12 noon
                    cal.set(Calendar.HOUR_OF_DAY, 12);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                }


                chatResponse.onSuccess(new Params(d, status, "Well, what's the appointment's description ? ",""));
                appointmentDescription = "description";

            }
            else if(action.equals("input.unknown") || action.equals("search")){
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, requestMessage);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
            else{
                if(aiResponse.getResult().getFulfillment().getSpeech().isEmpty()){
                    message = "Sorry, that didn't make sense to me.";
                }
                else{
                    message = aiResponse.getResult().getFulfillment().getSpeech();
                }
                chatResponse.onSuccess(new Params(d, status, message,""));
            }

        } catch (AIServiceException e) {
            message = "Something Went Wrong!";
            chatResponse.onSuccess(new Params(d, status, message,""));
            e.printStackTrace();
        }

    }

    public static void handleWeather(Context context, String name, final HTTPResponse httpResponse) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            // No Internet Connection
            httpResponse.onFailure(Constants.NO_INTERNET_CONNECTION, null);
            return;
        }

        try {
            String url = "/api/country";
            JSONObject body = new JSONObject();
            body.put("cityName", name);
            Log.e("BODY",body.toString());
            Shared.request(context, Request.Method.POST, url, body, Constants.AUTH_HEADERS, null, Constants.AES_ENCRYPTION, true, true, httpResponse);
        } catch (JSONException e) {
            // The app failed
            httpResponse.onFailure(Constants.APP_FAILURE, null);
            e.printStackTrace();
        }
    }


    public static void weatherForecast(String countryCode){
        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + cityName.toLowerCase() + "," + countryCode + "&cnt=" + duration + "&id=524901&APPID=88704427a46ca5ed706fdcd943b95cb9";
        try {
            final JsonObjectRequest weatherForeCastrequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject resp) {
                    try {
                        message = parseResponse(resp);
                        response.onSuccess(new Params(d, status, message,""));
                    } catch (JSONException e) {
                        response.onSuccess(new Params(d, status, "Something went wrong",""));
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    message = "Something went wrong";
                    response.onSuccess(new Params(d, status, message,""));
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
            response.onSuccess(new Params(d, status, "Something went wrong",""));
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 4: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Uri eventUri = activity.getApplicationContext().getContentResolver().insert(Uri.parse(eventUriString), eventValues);
                    if(needReminder.toLowerCase().startsWith("y")){
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put("event_id", eventID);
                        reminderValues.put("minutes", 5);
                        reminderValues.put("method", 1);
                        Uri reminderUri = activity.getApplicationContext().getContentResolver().insert(Uri.parse(reminderUriString), reminderValues);
                    }
                    needReminder = "";
                    response.onSuccess(new Params(d,status,"Appointment set !",""));
                }
            }
        }
    }

}


