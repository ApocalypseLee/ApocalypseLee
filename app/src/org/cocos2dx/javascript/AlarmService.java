package org.cocos2dx.javascript;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import static android.content.ContentValues.TAG;
/**
 * @author liyihe
 */
public class AlarmService extends Service {
    private Handler mHandler = new Handler();

    public AlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("com.fu");
                intent.putExtra("msg", "新消息1");
                AlarmService.this.sendBroadcast(intent);
                Log.d(TAG, "第一次发送广播");
            }
        }, 10000);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("com.fu");
                intent.putExtra("msg", "新消息2");
                AlarmService.this.sendBroadcast(intent);
                Log.d(TAG, "第二次发送广播");
            }
        }, 20000);
    }
}