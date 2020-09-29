package com.example.bt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    // BluetoothService 문자열 가져오기
    private final String TAG = BluetoothService.class.getName();

    // 프로파일 UUID
    public static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // 블루투스 상태를 나타내는 상수
    public static final int NONE = 0;
    public static final int LISTEN = 1;
    public static final int CONNECTING = 2;
    public static final int CONNECTED = 3;

    // 블루투스 어뎁터
    private BluetoothAdapter mBluetoothAdapter;
    // 연결 스레드 -> 소켓서버 연결
    private ConnectThread mConnectThread;
    // 연결 된 소켓을 관리하는 스레드 (읽기, 쓰기)
    private static ConnectedThread mConnectedThread;
    //
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private static Handler mHandler = null;

    // 블루투스의 현재 상태를 나는 변수
    public int mState = NONE;

    // 서비스가 액티비티에 바인딩 되는 순간 불러지는 함수
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mHandler = ((MyApplication) getApplication()).getHandler();
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    // 서비스 시작시 불러지는 함수
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        // 기본 안드로이드 블루투스 어뎁터를 가져옴
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null) {
            String deviceKey = intent.getStringExtra(BtConstants.DEVICE_KEY);
            if (deviceKey != null && deviceKey.length() > 0) {
                connectToDevice(deviceKey);
            } else {
                stopSelf();
            }
        }
        return START_REDELIVER_INTENT;
    }

    // 블루투스 연결 시작 부분
    private synchronized void connectToDevice(String macAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);

        // 연결
        this.connect(device);
    }

    //현재 인스턴스의 블루투스 상태 코드를 변경
    private void setState(int state) {
        mState = state;
        if (mHandler != null) {
            mHandler.obtainMessage(BtConstants.CHANGE_STATE, state, -1).sendToTarget();
        }
    }

    private static final Object obj = new Object();

    // 동기 함수 (한번에 하나의 일만 처리)
    public void write(byte[] out) {

        ConnectedThread r;

        synchronized (obj) {
            if (mState != CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }
    @Override
    public boolean stopService(Intent name) {
        this.stop();
        return super.stopService(name);
    }

    private void connectionFailed() {
        BluetoothService.this.stop();
        Message msg = mHandler.obtainMessage(BtConstants.TOAST_MESSAGE);
        Bundle bundle = new Bundle();
        bundle.putString(BtConstants.TOAST_MSG_KEY, "실패실패");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        BluetoothService.this.stop();
        Message msg = mHandler.obtainMessage(BtConstants.TOAST_MESSAGE);
        Bundle bundle = new Bundle();
        bundle.putString(BtConstants.TOAST_MSG_KEY, "실패실패");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        //만약 연결중인게 있으면 취소
        if (mState == CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // 연결된게 있으면 취소
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 연결 스레드 새로 만들고 시작
        mConnectThread = new ConnectThread(device, true);
        mConnectThread.start();
    }

    // 기존 연결 중단
    public synchronized void stop() {
        setState(NONE);
        if (mConnectThread != null) {
        mConnectThread.cancel();
        mConnectThread = null;
    }

        if (mConnectedThread != null) {
        mConnectedThread.cancel();
        mConnectedThread = null;
    }
        if (mSecureAcceptThread != null) {
        mSecureAcceptThread.cancel();
        mSecureAcceptThread = null;
    }

        if (mInsecureAcceptThread != null) {
        mInsecureAcceptThread.cancel();
        mInsecureAcceptThread = null;
    }

        if (mBluetoothAdapter != null) {
        mBluetoothAdapter.cancelDiscovery();
    }
    stopSelf();
    }

    private synchronized void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(BtConstants.NOTIFY_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BtConstants.NOTIFY_DEVICE_NAME_KEY, mmDevice.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            try {
                if (secure) {
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("sc",
                            mUUID);
                } else {
                    tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            "isc", mUUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "연결 실패", e);
            }
            mmServerSocket = tmp;
            setState(LISTEN);
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket;

            while (mState != CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "소켓 연결 실패", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case LISTEN:
                            case CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case NONE:
                            case CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "소켓 못 닫음", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "연결 스레드 끗!");
        }

        public void cancel() {
            Log.d(TAG, "서버소켓 닫기" + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "닫기 실패", e);
            }
        }
    }

    // 연결 스레드
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        // 생성자
        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                if (secure) {
                    // 블루투스 통신을 위한 소켓 생성
                    tmp = device.createRfcommSocketToServiceRecord(mUUID);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(mUUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "소켓 생성 실패", e);
            }
            mmSocket = tmp;
            setState(CONNECTING);
        }

        // 실행 부
        @Override
        public void run() {
            setName("ConnectThread");
            // 만약 블루투스 어댑터가 다른 블루투스 기기를 찾고 있을 경우 탐색 중지
            mBluetoothAdapter.cancelDiscovery();
            try {
                // 소켓에 연결
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    // 소켓에 연결이 실패한 경우 소켓 닫기
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "닫을 것도 없음", e2);
                }
                connectionFailed();
                return;

            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "닫는거 실패", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "임시 소켓 생성 실패", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            setState(CONNECTED);
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    mHandler.obtainMessage(BtConstants.READ_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (Exception e) {
                    connectionLost();
                    BluetoothService.this.stop();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                mHandler.obtainMessage(BtConstants.WRITE_MESSAGE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "쓰기 실패", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "소켓 닫기 실패", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        stop();
        Log.d(TAG, "파괴");
        super.onDestroy();
    }
}