package com.example.bt;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

// 앱이 시작되면 가장 먼저 생성되는 부분
public class MyApplication extends Application {
    Handler.Callback callback = null;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (callback != null) {
                callback.handleMessage(msg);
                return true;
            } else {
                return false;
            }
        }
    });
    public Handler getHandler() {
        return handler;
    }
    public void setCallBack(Handler.Callback callback) {
        this.callback = callback;
    }
}
