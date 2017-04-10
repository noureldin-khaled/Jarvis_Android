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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.responses.ChatResponse;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.VoiceRecognition;
import com.iot.guc.jarvis.responses.VoiceResponse;
import com.iot.guc.jarvis.adapters.ChatAdapter;
import com.iot.guc.jarvis.models.ChatMessage;
import com.iot.guc.jarvis.models.Params;
import com.iot.guc.jarvis.ChatAPI;
import org.json.JSONObject;
import java.util.ArrayList;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ChatFragment extends Fragment {

    private final AIConfiguration config = new AIConfiguration("97135bec060c48f9a6cc15dcf018ba2b",
        AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
    private EditText ChatFragment_EditText_Message;
    private ChatAdapter chatAdapter;
    private ListView ChatFragment_ListView_MessageList;
    private VoiceRecognition voiceRecognition;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ChatFragment_EditText_Message = (EditText) view.findViewById(R.id.ChatFragment_EditText_Message);
        ChatFragment_ListView_MessageList = (ListView) view.findViewById(R.id.ChatFragment_ListView_MessageList);
        final FloatingActionButton ChatFragment_FloatingActionButton_Send = (FloatingActionButton) view.findViewById(R.id.ChatFragment_FloatingActionButton_Send);
        ChatFragment_FloatingActionButton_Send.setOnClickListener(new View.OnClickListener() {
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
                    ChatFragment_FloatingActionButton_Send.setImageResource(R.drawable.ic_mic);
                else
                    ChatFragment_FloatingActionButton_Send.setImageResource(R.drawable.ic_send);
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

        ChatAPI.aiService = AIService.getService(getContext(), config);
        ChatAPI.aiDataService = new AIDataService(getContext(), config);
        ChatAPI.queue = Volley.newRequestQueue(getContext());
        getActivity().setTitle("Chat");
        return view;
    }


    public void sendMessage(String message) {
        if (message.isEmpty()) return;

        chatAdapter.add(new ChatMessage(message, true, Shared.getCurrentDate(), Shared.getCurrentTime()));
        chatAdapter.notifyDataSetChanged();

        new ChatAsyncTask().execute(message);
    }

    public void handleChat(Params result) {
        if (result.getMessage().isEmpty()) {
            // Send Request
            result.getDevice().handle(getContext(), result.getStatus(), new HTTPResponse() {
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
            chatAdapter.add(new ChatMessage(result.getMessage(), false, Shared.getCurrentDate(), Shared.getCurrentTime()));
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

            ChatAPI.handleChat(params[0],new ChatResponse() {
                @Override
                public void onSuccess(final Params param) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleChat(param);
                        }
                    });

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Params result) {
//            handleChat(result);
        }
    }

}


