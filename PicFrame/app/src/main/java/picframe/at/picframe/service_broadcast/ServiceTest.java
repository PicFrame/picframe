package picframe.at.picframe.service_broadcast;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
/*
public class ServiceTest extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private boolean isRunning;

    @Override
    public void onCreate() {
        Log.d(TAG, "Service - onCreate");
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service - onStartCommand");

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i<5; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) { }
                    if (i==3) {
                        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
                        Intent tmpIntent = new Intent();
                        tmpIntent.setAction(Keys.ACTION_FAILURE);
                        tmpIntent.putExtra(Keys.MSG_FAILURE, "I had round 3 now!");
                        lbm.sendBroadcast(tmpIntent);
                    }
                    if (isRunning) {
                        Log.d(TAG, "Service is running");
                    }
                }
                stopSelf();
            }
        }).start();

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service - onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.d(TAG, "Service - onDestroy");
    }
}*/
