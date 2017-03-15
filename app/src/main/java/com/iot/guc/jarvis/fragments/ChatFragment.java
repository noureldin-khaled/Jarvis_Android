package com.iot.guc.jarvis.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.HTTPResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.VoiceRecognition;
import com.iot.guc.jarvis.VoiceResponse;
import com.iot.guc.jarvis.adapters.ChatAdapter;
import com.iot.guc.jarvis.models.ChatMessage;
import com.iot.guc.jarvis.models.Device;

import org.json.JSONObject;

import java.util.ArrayList;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class ChatFragment extends Fragment {
    private final AIConfiguration config = new AIConfiguration("189b8c2169774040abec935b35f974d1",
            AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
    private AIService aiService;
    private AIDataService aiDataService;
    private EditText ChatFragment_EditText_Message;
    private ChatAdapter chatAdapter;
    private ListView ChatFragment_ListView_MessageList;
    private VoiceRecognition voiceRecognition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ChatFragment_EditText_Message = (EditText) view.findViewById(R.id.ChatFragment_EditText_Message);
        ChatFragment_ListView_MessageList = (ListView) view.findViewById(R.id.ChatFragment_ListView_MessageList);
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.ChatFragment_FloatingActionButton_Send);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ChatFragment_EditText_Message.getText().toString().isEmpty()) {
                    // Voice
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, Constants.PERMISSION_CODE);
                    else
                        voiceRecognition.listen();
                }
                else {
                    // Text
                    sendMessage(ChatFragment_EditText_Message.getEditableText().toString());
                    ChatFragment_EditText_Message.setText("");
                }
            }
        });


        ChatFragment_EditText_Message.addTextChangedListener(new TextWatcher() {
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

        chatAdapter = new ChatAdapter(getActivity(), new ArrayList<ChatMessage>());
        ChatFragment_ListView_MessageList.setAdapter(chatAdapter);

        voiceRecognition = new VoiceRecognition(getContext(), new VoiceResponse() {
            @Override
            public void onSuccess(String result) {
                sendMessage(result);
            }

            @Override
            public void onError(int error) {
                chatAdapter.add(new ChatMessage("I Can\'t hear you!", false, Shared.getCurrentDate(), Shared.getCurrentTime()));
                chatAdapter.notifyDataSetChanged();
            }
        });

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case Constants.PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    voiceRecognition.listen();
                }
            }
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


