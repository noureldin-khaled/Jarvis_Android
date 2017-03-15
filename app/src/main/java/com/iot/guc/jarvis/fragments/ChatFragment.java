package com.iot.guc.jarvis.fragments;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.VoiceRecognition;
import com.iot.guc.jarvis.VoiceResponse;
import com.iot.guc.jarvis.adapters.ChatAdapter;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.models.ChatMessage;
import com.iot.guc.jarvis.models.Device;

import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.android.AIService;
import ai.api.android.AIDataService;

public class ChatFragment extends Fragment implements View.OnClickListener{

    private EditText msg_edittext;
    private ArrayList<ChatMessage> chatlist;
    private ChatAdapter chatAdapter;
    private AIService aiService;
    private AIDataService aiDataService;
    private AIConfiguration config = new AIConfiguration("189b8c2169774040abec935b35f974d1",
            AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
    private ListView msgListView;
    private String resultMessage = "";
    private String status = "none";
    private int deviceId = 1;   // TODO: 2017-03-11 remove hardcoding after filtering on rooms
    private ChatMessage chatMessage;
    private VoiceRecognition voiceRecognition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        msg_edittext = (EditText) view.findViewById(R.id.messageEditText);
        msgListView = (ListView) view.findViewById(R.id.msgListView);
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.sendMessageButton);
        fab.setOnClickListener(this);

        voiceRecognition = new VoiceRecognition(getContext(), new VoiceResponse() {
            @Override
            public void onSuccess(String result) {
                sendTextMessage(null);
            }

            @Override
            public void onError(int error) {
                Log.i(getActivity().getLocalClassName(), "onError: " + error);
            }
        });

        msg_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty())
                    fab.setImageResource(android.R.drawable.ic_btn_speak_now);
                else
                    fab.setImageResource(android.R.drawable.ic_media_play);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chatlist = new ArrayList<ChatMessage>();
        chatAdapter = new ChatAdapter(getActivity(), chatlist);
        msgListView.setAdapter(chatAdapter);

        aiService = AIService.getService(getContext(), config);
        aiDataService = new AIDataService(getContext(), config);
        getActivity().setTitle("Chat");
        return view;
    }


    public void sendTextMessage(View v) {
        String message = msg_edittext.getEditableText().toString();
        if (!message.equalsIgnoreCase("")) {
            chatMessage = new ChatMessage(message, true, Shared.getCurrentDate(), Shared.getCurrentTime());
            msg_edittext.setText("");
            chatAdapter.add(chatMessage);
            chatAdapter.notifyDataSetChanged();

            new ChatAsyncTask().execute(message);
        }
    }

    public void receiveTextMessage(String message) {

        if(!status.equals("none")){
            try{
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                String url = Shared.getServer().URL() + "/api/device/" + deviceId;

                JSONObject body = new JSONObject();
                body.put("status", status);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    resultMessage = response.get("message").toString();
                                    chatMessage = new ChatMessage(resultMessage, false, Shared.getCurrentDate(), Shared.getCurrentTime());
                                    status = "none";
                                    chatAdapter.add(chatMessage);
                                    chatAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                resultMessage = error.toString();
                                chatMessage = new ChatMessage(resultMessage, false, Shared.getCurrentDate(), Shared.getCurrentTime());
                                status = "none";
                                chatAdapter.add(chatMessage);
                                chatAdapter.notifyDataSetChanged();
                            }
                        })
                        {

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<>();
                                headers.put("Authorization", Shared.getAuth().getToken());
                                return headers;
                            }
                        };

                queue.add(request);

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            chatMessage = new ChatMessage(message, false, Shared.getCurrentDate(), Shared.getCurrentTime());
            chatAdapter.add(chatMessage);
            chatAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton: {
                if (msg_edittext.getText().toString().isEmpty()) {
                    // Voice
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, Constants.PERMISSION_CODE);
                    }
                    else {
                        voiceRecognition.listen();
                    }
                }
                else {
                    // Text

                }
//                sendTextMessage(v);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(getActivity().getLocalClassName(), "onRequestPermissionsResult: ");
        switch(requestCode) {
            case Constants.PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    voiceRecognition.listen();
                }
            }
        }
    }

    private class ChatAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(params[0]);
                aiService.textRequest(aiRequest);
                AIResponse aiResponse = aiDataService.request(aiRequest);

                ArrayList<Device> devices = Shared.getDevices();
                //TODO add filters on rooms
//                for(Device device : devices){
//                    if(device.getType()== Device.TYPE.LIGHT_BULB){
//                        deviceId = device.getId();
//                        break;
//                    }
//                }

                if(aiResponse.getResult().getAction().equals("LightsOn")){
                    status = "true";

                }
                else if(aiResponse.getResult().getAction().equals("LightsOff")){
                    status = "false";
                }
                else{
                    return aiResponse.getResult().getFulfillment().getSpeech();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
                receiveTextMessage(result);
        }

    }

    public EditText getMsg_edittext() {
        return msg_edittext;
    }

    public void setMsg_edittext(EditText msg_edittext) {
        this.msg_edittext = msg_edittext;
    }

    public ArrayList<ChatMessage> getChatlist() {
        return chatlist;
    }

    public void setChatlist(ArrayList<ChatMessage> chatlist) {
        this.chatlist = chatlist;
    }

    public ChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    public void setChatAdapter(ChatAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }

    public AIService getAiService() {
        return aiService;
    }

    public void setAiService(AIService aiService) {
        this.aiService = aiService;
    }

    public AIDataService getAiDataService() {
        return aiDataService;
    }

    public void setAiDataService(AIDataService aiDataService) {
        this.aiDataService = aiDataService;
    }

    public AIConfiguration getConfig() {
        return config;
    }

    public void setConfig(AIConfiguration config) {
        this.config = config;
    }

    public ListView getMsgListView() {
        return msgListView;
    }

    public void setMsgListView(ListView msgListView) {
        this.msgListView = msgListView;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}


