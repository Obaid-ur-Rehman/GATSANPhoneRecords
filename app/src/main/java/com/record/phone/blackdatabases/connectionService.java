package com.record.phone.blackdatabases;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by XSS on 14/08/2017.
 */

public class connectionService extends Service {

    static TCPClient con = null;
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                con.sendMessage(extras.getString("data"));
            }
        }catch (Exception e){}

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        new ConnectTask().execute("");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public class ConnectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object
            con = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });

            con.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            try {
                String packet = values[0];
                //packet = packet.substring(1);
                if(packet.charAt(0) == 'm')
                {
                    String resp = packet.substring(1);
                    if(resp.length() < 5)
                    {
                        cell_lookup.output.setText("No record found!");
                    }
                    else {
                        cell_lookup.showOutput(resp);
                    }
                }
                else if(packet.charAt(0) == 'c')
                {
                    cnic_lookup.output.setText(packet.substring(1)+ "\n\nSearch completed\n\n");
                }
                else if(packet.charAt(0) == 'I')
                {
                    cell_lookup.output.setText("Please wait...\nFetching records\n\n\n");
                    cnic_lookup.output.setText("Please wait...\nFetching records\n\n\n");
                }
                else
                {
                    if(packet.charAt(0) == 'S' && packet.charAt(1) == 'P')
                    {
                        cell_lookup.output.setText(packet.substring(1));
                        cnic_lookup.output.setText(packet.substring(1));
                    }
                }
            }catch (Exception e)
            {
            }


            //response received from server
            //process server response here....

        }
    }


}
