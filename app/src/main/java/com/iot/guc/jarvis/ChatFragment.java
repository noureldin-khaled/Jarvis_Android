package com.iot.guc.jarvis;


import java.util.ArrayList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.android.AIService;
import ai.api.android.AIDataService;

public class ChatFragment extends Fragment implements View.OnClickListener{

    private EditText msg_edittext;
    public static ArrayList<ChatMessage> chatlist;
    public static ChatAdapter chatAdapter;
    public static AIService aiService;
    public static AIDataService aiDataService;
    public static AIConfiguration config = new AIConfiguration("189b8c2169774040abec935b35f974d1",
            AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);
    ListView msgListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        msg_edittext = (EditText) view.findViewById(R.id.messageEditText);
        msgListView = (ListView) view.findViewById(R.id.msgListView);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.sendMessageButton);
        fab.setOnClickListener(this);

        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        chatlist = new ArrayList<ChatMessage>();
        chatAdapter = new ChatAdapter(getActivity(), chatlist);
        msgListView.setAdapter(chatAdapter);

        aiService = AIService.getService(getContext(), config);
        aiDataService=new AIDataService(getContext(), config);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public void sendTextMessage(View v) {
        String message = msg_edittext.getEditableText().toString();
        if (!message.equalsIgnoreCase("")) {
            ChatMessage chatMessage = new ChatMessage(message, true, Common.getCurrentDate(), Common.getCurrentTime());
            msg_edittext.setText("");
            chatAdapter.add(chatMessage);
            chatAdapter.notifyDataSetChanged();

            new ChatAsyncTask().execute(message);
        }
    }

    public void receiveTextMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, false, Common.getCurrentDate(), Common.getCurrentTime());
        msg_edittext.setText("");
        chatAdapter.add(chatMessage);
        chatAdapter.notifyDataSetChanged();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton:
                sendTextMessage(v);

        }
    }

    private class ChatAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {


            try {
                AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(params[0]);
                aiService.textRequest(aiRequest);
                final AIResponse aiResponse = aiDataService.request(aiRequest);
                switch(aiResponse.getResult().getAction()){
                    case "LightsOff" : return "Turning off the lights";
                    case "LightsOn"  : return "Turning on the lights";
                    default: return aiResponse.getResult().getFulfillment().getSpeech();
                }

            } catch (AIServiceException e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            receiveTextMessage(result);
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

}


