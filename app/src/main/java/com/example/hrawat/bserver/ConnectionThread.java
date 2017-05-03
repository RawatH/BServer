package com.example.hrawat.bserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

/**
 * Created by hrawat on 26-04-2017.
 */

public class ConnectionThread extends Thread {

    private Messenger messenger;
    private BluetoothServerSocket bluetoothServerSocket;
    private static final String TAG = "acceptthread";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket clientSocket;


    public ConnectionThread(Messenger messenger) {
        this.messenger = messenger;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Utitlity.NAME, Utitlity.uuid);
            sendMessageToService(BServerMessage.SERVER_SOCKET_CREATED);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        bluetoothServerSocket = tmp;
    }


    public void run() {
        BluetoothSocket socket = null;

        while (true) {
            try {
                sendMessageToService(BServerMessage.MSG_WAITING_FOR_CLIENT);
                socket = bluetoothServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }


            if (socket != null) {
                clientSocket = socket;
                sendMessageToService(BServerMessage.MSG_CLIENT_CONNECTED);
            }

        }

    }

    public void cancel() {
        try {
            bluetoothServerSocket.close();
            sendMessageToService(BServerMessage.MSG_STOP_CONNECTION_THREAD);
        } catch (IOException e) {

        }
    }


    private void sendMessageToService(int msg) {

        Message message = Message.obtain();
        message.what = msg;
        Bundle bundle = new Bundle();
        switch (msg) {

            case BServerMessage.MSG_CLIENT_CONNECTED:
                bundle.putSerializable("data", new DataVO(clientSocket));
                message.setData(bundle);
                break;

            case BServerMessage.SERVER_SOCKET_CREATED:

                break;
        }

        try {
            messenger.send(message);
        } catch (RemoteException re) {

        }

    }
}
