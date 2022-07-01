package com.findbulous.pos.Network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    @Override
    public void onCreate(){
        super.onCreate();
        Log.d("inside", "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implement");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //handler.post(periodicUpdate);
        return START_STICKY;
    }

    public boolean isOnline(Context c){
        if(NetworkUtils.isNetworkAvailable(c)) {
            return true;
        }else{
            return  false;
        }
    }

//    Handler handler = new Handler();
//    private Runnable periodicUpdate = new Runnable() {
//        @Override
//        public void run() {
//            handler.postDelayed(periodicUpdate,
//                    1*1000 - SystemClock.elapsedRealtime()%1000);
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction(MyReceiver.BroadcastStringForAction);
//            broadcastIntent.putExtra("online_status", ""+isOnline(MyService.this));
//            sendBroadcast(broadcastIntent);
//        }
//    };
}
