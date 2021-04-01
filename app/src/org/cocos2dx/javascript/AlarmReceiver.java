package org.cocos2dx.javascript;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * @author liyihe
 */
public class AlarmReceiver extends BroadcastReceiver {

    private final String TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive: 收到广播");
        Log.d(TAG, intent.getAction());
        //拿到传来过来数据
        String msg = intent.getStringExtra("msg");
        //拿到锁屏管理者
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.isKeyguardLocked()) {   //为true就是锁屏状态下
            //启动Activity
            Intent alarmIntent = new Intent(context, AlarmActivity.class);
            //携带数据
            alarmIntent.putExtra("msg", msg);
            //activity需要新的任务栈
            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alarmIntent);
        }
    }
}