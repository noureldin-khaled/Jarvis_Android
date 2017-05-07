package com.iot.guc.jarvis;

import android.os.AsyncTask;

import com.iot.guc.jarvis.models.Server;
import com.iot.guc.jarvis.responses.ServerResponse;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerTask {
    private ServerResponse response;

    public ServerTask(ServerResponse response) {
        this.response = response;
    }

    public void connectToServer() {
        try {
            byte[] message = new byte[1000];
            DatagramPacket dp = new DatagramPacket(message, message.length, InetAddress.getByName("255.255.255.255"), 9000);
            DatagramSocket s = new DatagramSocket();
            s.send(dp);
            int p = s.getLocalPort();
            s.close();

            s = new DatagramSocket(p);
            dp = new DatagramPacket(message, message.length);
            s.setSoTimeout(2500);
            s.receive(dp);
            String[] response = new String(message, 0, dp.getLength()).split("::");
            s.close();
            String ip = response[0];
            int port = Integer.parseInt(response[1]);
            Shared.setServer(new Server(ip, port));
        } catch (IOException e) {
            connectToServer();
        }
    }

    public void start() {
        if (Shared.getServer() != null) {
            response.onFinish();
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                connectToServer();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                response.onFinish();
            }
        }.execute();
    }
}
