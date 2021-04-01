package org.cocos2dx.javascript;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
/**
 * @author liyihe
 */
public class HookReceiver extends BroadcastReceiver {

    private final String TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub
        Log.e(TAG, "接收到广播：" + intent.getAction());

        if (intent.getAction() == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
            preferGlobalHome(500);
        }
    }

    private static android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
    /**
     * 返回主页
     *
     * @param delay
     */
    public static void preferGlobalHome(long delay) {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        SDKWrapper.getInstance().getContext().startActivity(home);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                SDKWrapper.getInstance().getContext().startActivity(home);
            }
        }, delay);
    }
}