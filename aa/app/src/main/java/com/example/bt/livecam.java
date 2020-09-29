package com.example.bt;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class livecam extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livecam);

        final WebView webView = (WebView)findViewById(R.id.webView);
        webView.setPadding(0,0,0,0);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);// 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
        webView.getSettings().setUseWideViewPort(true);  // wide viewport를 사용하도록 설정

        String url ="http://192.168.0.86:8091/?action=stream";
        //이렇게 비디오만 가져올 수 있어야 한다.
        //밑에있는 주소로 했을 경우 웹 전체가 나와서 구지다
        //"http://192.168.219.103:8091/javascript_simple.html";
        webView.loadUrl(url);


    }
}