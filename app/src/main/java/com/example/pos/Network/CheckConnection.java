package com.example.pos.Network;

import android.content.Context;
import android.util.Log;

import java.util.TimerTask;

public class CheckConnection extends TimerTask {
    private Context context;
    public CheckConnection(Context context){
        this.context = context;
    }
    public void run() {
        if(NetworkUtils.isNetworkAvailable(context)){
            //CONNECTED
            Log.d("Wifi Tagggg", "Connectedddddddddddddd");
        }else {
            //DISCONNECTED
            Log.d("Wifi Tagggg", "Wifi Losssssssssssssss");
        }
    }
}
