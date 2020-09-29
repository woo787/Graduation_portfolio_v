package com.example.bt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SubMain extends AppCompatActivity {
    private Button co_btn3;
    private Button co_btn2;
    private Button co_btn1;
    private Button bot_bt;
    private ImageButton setting;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private TextView text6;
    private TextView text7;
    private TextView text8;
    private TextView text10;
    private TextView text12;
    private TextView text14;



    int i = 100;

    // 소켓통신에 필요한것
    private String html = "";
    private Handler mHandler;
    private BufferedReader br;
    private Socket socket;
    private DataOutputStream dos;
    //private DataInputStream dis;
    private MyThread checkUpdate;
    private String ip = "192.168.219.101";            // IP 번호
    private int port = 8082;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_main);

        co_btn1 = findViewById(R.id.co_btn1);
        co_btn2 = findViewById(R.id.co_btn2);
        co_btn3 = findViewById(R.id.co_btn3);

        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);

        text6 = (TextView)findViewById(R.id.text6);
        text7 = (TextView)findViewById(R.id.text7);
        text8 = (TextView)findViewById(R.id.text8);
        text10 = (TextView)findViewById(R.id.text10);
        text12 = (TextView)findViewById(R.id.text12);
        text14 = (TextView)findViewById(R.id.text14);

        bot_bt = findViewById(R.id.bot_bt);

        bot_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubMain.this, livecam.class);
                startActivity(intent);
            }
        });

        iv1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN) {
                    // 터치했을 때의 이미지 변경
                    co_btn1.setBackgroundResource(R.drawable.co_btn_act);
                    co_btn2.setBackgroundResource(R.drawable.co_btn);
                    co_btn3.setBackgroundResource(R.drawable.co_btn);
                    Intent intent = new Intent(SubMain.this, Voice.class);
                    startActivity(intent);
                }

                return false;
            }
        });

        iv2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 터치했을 때의 이미지 변경
                    co_btn2.setBackgroundResource(R.drawable.co_btn_act);
                    co_btn1.setBackgroundResource(R.drawable.co_btn);
                    co_btn3.setBackgroundResource(R.drawable.co_btn);
                    Intent intent = new Intent(SubMain.this, Main2Activity.class);
                    startActivity(intent);
                }
                return false;
            }
        });

        setting = findViewById(R.id.setting);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubMain.this, MainActivity.class);
                startActivity(intent);
            }
        });

        iv3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    // 터치했을 때의 이미지 변경
                    co_btn3.setBackgroundResource(R.drawable.co_btn_act);
                    co_btn1.setBackgroundResource(R.drawable.co_btn);
                    co_btn2.setBackgroundResource(R.drawable.co_btn);

                    //처음엔 무조건 welcome my master로 초기화
                    // tcp통신될때만 unauthorized만 켜지게 할 것
                    text6.setVisibility(View.INVISIBLE);
                    text7.setVisibility(View.INVISIBLE);
                    text8.setVisibility(View.VISIBLE);


                    //위함수를 통햬 텍스트뷰를 제거할 수있다. 반대로 보고싶을땐 VISIBLE하면 된다.
                }
                return false;
            }
        });

        mHandler = new Handler();
        checkUpdate = new MyThread();
        checkUpdate.start();


    }
    class MyThread extends Thread {
        boolean connected = false;

        public void run() {
            while (!connected) {
                socket = new Socket();
                //connected = socket.isConnected() && !socket.isClosed();
                Log.w("connect", "연결 하는중");
                // ip받기
                //String newip = String.valueOf(ip_edit.getText());
                String newip = "192.168.0.86";
                // 서버 접속
                try {
                    //socket = new Socket(newip, port);
                    SocketAddress addr = new InetSocketAddress(newip, port);
                    socket.connect(addr);
                    //connected = true;
                    Log.w("서버 접속됨", "서버 접속됨");

                } catch (IOException e1) {
                    Log.w("서버접속못함", "서버접속못함");

                    e1.printStackTrace();
                }

                try {
                    dos = new DataOutputStream(socket.getOutputStream());   // output에 보낼꺼 넣음
                    //dis = new DataInputStream(socket.getInputStream());
                    // input에 받을꺼 넣어짐
                    dos.writeUTF("안드로이드에서 서버로 연결요청");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.w("버퍼", "버퍼생성 잘못됨");
                }
                Log.w("버퍼", "버퍼생성 잘됨");

                // 서버에서 계속 받아옴 - 한번은 문자, 한번은 숫자를 읽음. 순서 맞춰줘야 함.
                try {
                    byte[] byteArr = new byte[1024];
                    InputStream is = socket.getInputStream();
                    int readByteCount = is.read(byteArr);
                    final String data = new String(byteArr, 0, readByteCount, "UTF-8");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            --i;
                            final Toast toast = Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG);
                            // Toast 를 이용하여 알림창을 띄운다
                            toast.show();   // show(); 꼭 붙여줘야 실행이 된다
                            text6.setVisibility(View.VISIBLE);
                            text7.setVisibility(View.INVISIBLE);
                            text8.setVisibility(View.INVISIBLE);
                            //안정도 체크
                            //처음엔 무조건 Super Secure
                            //점점 TCP통신이 될 때마다 안정도를 낮춘다.
                            if(i==99)
                            {
                                text14.setVisibility(View.VISIBLE);
                                text12.setVisibility(View.INVISIBLE);
                                text10.setVisibility(View.INVISIBLE);
                            }
                            else if(i==75)
                            {
                                text14.setVisibility(View.INVISIBLE);
                                text12.setVisibility(View.VISIBLE);
                                text10.setVisibility(View.INVISIBLE);
                            }
                            else if(i==50)
                            {
                                text14.setVisibility(View.INVISIBLE);
                                text12.setVisibility(View.INVISIBLE);
                                text10.setVisibility(View.VISIBLE);
                            }


                            //사이렌 음악까지 켜준다.
                            /*mediaPlayer =MediaPlayer.create(getApplicationContext(),R.raw.siren);
                            long start = System.currentTimeMillis();
                            long end = start + 3*1000; // 3seconds * 1000 ms/sec
                            while (System.currentTimeMillis() < end)
                            {
                                mediaPlayer.start();
                            }
                            mediaPlayer.stop();*/

                        }
                    });

                    socket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }
}

