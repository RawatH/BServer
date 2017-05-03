package com.example.hrawat.bserver;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by hrawat on 26-04-2017.
 */

public class DataTrasnferThread extends Thread {

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private static final String TAG = "connectionthread";
    private String dataMessage = "";

    private Messenger messenger;

    public DataTrasnferThread(BluetoothSocket socket, Messenger messenger) {
        this.messenger = messenger;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.

                numBytes = mmInStream.read(mmBuffer);

                dataMessage = new String(mmBuffer);
                // Send the obtained bytes to the UI activity.
                sendMessageToService(BServerMessage.MSG_FROM_CLIENT);
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }


    public void sendMessage(String data) {
        try {
            byte[] bytes = data.getBytes();
            mmOutStream.write(bytes);
            // Share the sent message with the UI activity.
            sendMessageToService(BServerMessage.MSG_SENT_TO_CLIENT);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
            sendMessageToService(BServerMessage.BROKEN_PIPE);
        }
    }

    private void sendMessageToService(int msg) {

        Message message = Message.obtain();
        message.what = msg;

        switch (msg) {
            case BServerMessage.MSG_FROM_CLIENT:
                Bundle bundle = new Bundle();
                bundle.putString("msg", dataMessage.toString());
                message.setData(bundle);
                mmBuffer = new byte[1024];
                break;
        }

        try {
            messenger.send(message);
        } catch (RemoteException re) {

        }

    }

}
