package com.iot.guc.jarvis.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.adapters.ChatAdapter;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.models.ChatMessage;
import com.iot.guc.jarvis.models.Device;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.android.AIService;
import ai.api.android.AIDataService;

public class ChatFragment extends Fragment {
    private final AIConfiguration config = new AIConfiguration("189b8c2169774040abec935b35f974d1",
            AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
    private AIService aiService;
    private AIDataService aiDataService;
    private EditText ChatFragment_EditText_Message;
    private ChatAdapter chatAdapter;
    private ListView ChatFragment_ListView_MessageList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ChatFragment_EditText_Message = (EditText) view.findViewById(R.id.ChatFragment_EditText_Message);
        ChatFragment_ListView_MessageList = (ListView) view.findViewById(R.id.ChatFragment_ListView_MessageList);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.ChatFragment_FloatingActionButton_Send);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(ChatFragment_EditText_Message.getEditableText().toString());
                ChatFragment_EditText_Message.setText("");
            }
        });

        chatAdapter = new ChatAdapter(getActivity(), new ArrayList<ChatMessage>());
        ChatFragment_ListView_MessageList.setAdapter(chatAdapter);

        aiService = AIService.getService(getContext(), config);
        aiDataService = new AIDataService(getContext(), config);

        return view;
    }


    public void sendMessage(String message) {
        if (message.isEmpty()) return;

        chatAdapter.add(new ChatMessage(message, true, Shared.getCurrentDate(), Shared.getCurrentTime()));
        chatAdapter.notifyDataSetChanged();

        new ChatAsyncTask().execute(message);
    }

    public void handleChat(Params result) {
        if (result.message.isEmpty()) {
            // Send Request
            result.device.handle(getContext(), result.status, new HTTPResponse() {
                @Override
                public void onSuccess(int statusCode, JSONObject body) {
                    chatAdapter.add(new ChatMessage("Done.", false, Shared.getCurrentDate(), Shared.getCurrentTime()));
                    chatAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int statusCode, JSONObject body) {
                    String response;
                    switch (statusCode) {
                        case Constants.NO_INTERNET_CONNECTION: {
                            response = "No Internet Connection!";
                        }
                        break;
                        case Constants.SERVER_NOT_REACHED: {
                            response = "Server Can\'t Be Reached!";
                        }
                        break;
                        default: {
                            response = "Something Went Wrong!";
                        }
                    }

                    chatAdapter.add(new ChatMessage(response, false, Shared.getCurrentDate(), Shared.getCurrentTime()));
                    chatAdapter.notifyDataSetChanged();
                }
            });
        }
        else {
            // Not a command or got an error
            chatAdapter.add(new ChatMessage(result.message, false, Shared.getCurrentDate(), Shared.getCurrentTime()));
            chatAdapter.notifyDataSetChanged();
        }
    }

    private class ChatAsyncTask extends AsyncTask<String, Void, Params> {

        @Override
        protected Params doInBackground(String... params) {
            boolean status = false;
            String message = "";
            Device d = null;
            try {
                AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(params[0]);
                aiService.textRequest(aiRequest);
                AIResponse aiResponse = aiDataService.request(aiRequest);

                //TODO add filters on rooms
                if(aiResponse.getResult().getAction().startsWith("Lights")){
                    for(Device device : Shared.getDevices()){
                        if(device.getType() == Device.TYPE.LIGHT_BULB){
                            d = device;
                            break;
                        }
                    }
                }

                if(aiResponse.getResult().getAction().equals("LightsOn")){
                    status = true;
                }
                else if(aiResponse.getResult().getAction().equals("LightsOff")){
                    status = false;
                }
                else{
                    message = aiResponse.getResult().getFulfillment().getSpeech();
                }

            } catch (AIServiceException e) {
                message = "Something Went Wrong!";
                e.printStackTrace();
            }

            return new Params(d, status, message);
        }

        @Override
        protected void onPostExecute(Params result) {
            handleChat(result);
        }
    }

    private static class Params {
        Device device;
        boolean status;
        String message;

        public Params(Device device, boolean status, String message) {
            this.device = device;
            this.status = status;
            this.message = message;
        }
    }
}


