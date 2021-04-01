package org.cocos2dx.javascript;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.pinball.games.R;

import static android.content.ContentValues.TAG;

/**
 * @author liyihe
 */
public class AlarmActivity extends Activity {
    TextView textview;
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AlarmActivity.this, AppActivity.class);
            startActivity(intent);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);
        textview = getRes(R.id.textView, clickListener);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        //下面就是根据自己的跟你需求来写，跟写一个Activity一样的
        //拿到传过来的数据
        String msg = getIntent().getStringExtra("msg");
        textview.setText("收到消息:" + msg);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 这个方法是当这个activity没有销毁的时候，人为的按下锁屏键，然后再启动这个Activity的时候会去调用
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent: 调用");
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isScreenOn()) {
            String msg = intent.getStringExtra("msg");
            textview.setText("又收到消息:" + msg);
            //点亮屏幕
            @SuppressLint
                    ("InvalidWakeLockTag") PowerManager.WakeLock wl =
                    pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire();
            wl.release();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public TextView getRes(int id, View.OnClickListener clickListener) {
        if (id > 0) {
            TextView textView = findViewById(id);
            if (clickListener != null) textView.setOnClickListener(clickListener);
            return textView;
        }
        return null;
    }
}
