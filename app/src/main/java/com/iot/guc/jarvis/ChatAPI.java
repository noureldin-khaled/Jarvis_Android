package com.iot.guc.jarvis;

/**
 * Created by MariamMazen on 2017-04-02.
 */

import android.util.Log;

import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.models.Params;

import java.util.ArrayList;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class ChatAPI {

//    private final AIConfiguration config = new AIConfiguration("97135bec060c48f9a6cc15dcf018ba2b",
//            AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
    public static AIService aiService;
    public static AIDataService aiDataService;
    static boolean incompleteLight = false;
    static String incompleteLightMessage = "";

    public static Params handleChat(String requestMessage) {

        boolean status = false;
        String message = "";
        Device d = null;
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
            } else {
                message = aiResponse.getResult().getFulfillment().getSpeech();
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
        } catch (AIServiceException e) {
            message = "Something Went Wrong!";
            e.printStackTrace();
        }

        return new Params(d, status, message);
    }
}
