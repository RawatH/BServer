package com.example.hrawat.bserver;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by hrawat on 25-04-2017.
 */

public class BTServerService extends Service {

    public static final String TAG = "BServer";
    private Messenger activityMessenger;
    private ConnectionThread connectionThread;
    private DataTrasnferThread dataTrasnferThread;
    private Bundle clientBundle = null;
    private Messenger messenger = new Messenger(new IncomingHandler());
    private int clientCount;



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        connectionThread.cancel();
        return super.onUnbind(intent);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle;
            switch (msg.what) {
                case BServerMessage.MSG_HANDSHAKE:
                    bundle = msg.getData();
                    activityMessenger = bundle.getParcelable("messenger");
                    connectionThread = new ConnectionThread(messenger);
                    connectionThread.start();
                    break;

                case BServerMessage.SERVER_SOCKET_CREATED:
                    sendMessageToActivity(BServerMessage.SERVER_SOCKET_CREATED);
                    break;

                case BServerMessage.MSG_CLIENT_CONNECTED:
                    clientCount++;
                    if (dataTrasnferThread == null) {
                        BluetoothSocket clientSocket = (BluetoothSocket) ((DataVO) msg.getData().getSerializable("data")).getObject();
                        dataTrasnferThread = new DataTrasnferThread(clientSocket, messenger);
                        dataTrasnferThread.start();
                    }
                    sendMessageToActivity(BServerMessage.MSG_CLIENT_CONNECTED);
                    break;

                case BServerMessage.MSG_FROM_CLIENT:
                    clientBundle = msg.getData();
                    sendMessageToActivity(BServerMessage.MSG_FROM_CLIENT);
                    break;

                case BServerMessage.SEND_MSG_TO_CLIENT:

                    if(dataTrasnferThread != null){
                        dataTrasnferThread.sendMessage(msg.getData().getString("msg"));
                    }
                    break;

                case BServerMessage.MSG_STOP_CONNECTION_THREAD:
                    sendMessageToActivity(BServerMessage.MSG_STOP_CONNECTION_THREAD);
                    break;
                case BServerMessage.MSG_WAITING_FOR_CLIENT:
                    sendMessageToActivity(BServerMessage.MSG_WAITING_FOR_CLIENT);
                    break;

                case BServerMessage.BROKEN_PIPE:
                    sendMessageToActivity(BServerMessage.BROKEN_PIPE);
                    break;

            }
        }
    }


    private void sendMessageToActivity(int msg) {
        Message message = Message.obtain();
        message.what = msg;
        Bundle bundle;
        switch (msg) {
            case BServerMessage.MSG_CLIENT_COUNT:
                bundle = new Bundle();
                bundle.putInt("count" , clientCount);
                message.setData(bundle);
                break;
            case BServerMessage.MSG_FROM_CLIENT:
                message.setData(clientBundle);
                break;
        }

        try {
            activityMessenger.send(message);
        } catch (RemoteException re) {

        }
    }


}
