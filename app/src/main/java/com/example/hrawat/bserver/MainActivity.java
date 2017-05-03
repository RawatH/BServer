package com.example.hrawat.bserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private Button startServer;
    private Button stopServer;
    private Button clearLogs;
    private TextView logText;
    private TextView serverStatus;
    private EditText clientMsg;
    private boolean isBound = false;

    private Messenger serverMessenger;
    private Messenger messenger = new Messenger(new IncomingHandler());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clientMsg = (EditText) findViewById(R.id.clientMsg);
        startServer = (Button) findViewById(R.id.startServer);
        stopServer = (Button) findViewById(R.id.stopServer);
        clearLogs = (Button) findViewById(R.id.clearLogs);

        startServer.setOnClickListener(this);
        stopServer.setOnClickListener(this);
        clearLogs.setOnClickListener(this);

        logText = (TextView) findViewById(R.id.log);
        logText.setMovementMethod(new ScrollingMovementMethod());
        serverStatus = (TextView) findViewById(R.id.serverStatus);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mReceiver, filter);

        if (BluetoothAdapter.getDefaultAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(discoverableIntent);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serverMessenger = new Messenger(service);
        addLog("Service Connected");
        sendMessageToService(BServerMessage.MSG_HANDSHAKE);
        isBound = true;
        stopServer.setEnabled(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startServer:
                if(!isBound) {
                    Intent intent = new Intent(this, BTServerService.class);
                    bindService(intent, this, Context.BIND_AUTO_CREATE);
                    startServer.setEnabled(false);
                    stopServer.setEnabled(true);
                }
                break;
            case R.id.stopServer:
                if(isBound) {
                    isBound = false;
                    stopServer.setEnabled(false);
                    startServer.setEnabled(true);
                    unbindService(this);
                }
                break;
            case R.id.clearLogs:
                logText.setText("");
                break;
            case R.id.sendMsg:
                sendMessageToService(BServerMessage.SEND_MSG_TO_CLIENT);
                clientMsg.setText("");
                break;
        }
    }

    private void addLog(String log) {
        logText.setText(log + "\n" + logText.getText());
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {

                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1)) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        serverStatus.setText("Discovery ON");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        serverStatus.setText("Discovery OFF");
                        break;
                }
            }
        }
    };

    private void sendMessageToService(int msg) {
        Message message = Message.obtain();
        message.what = msg;
        Bundle bundle;
        switch (msg) {
            case BServerMessage.MSG_HANDSHAKE:
                bundle = new Bundle();
                bundle.putParcelable("messenger", messenger);
                message.setData(bundle);
                break;

            case BServerMessage.SEND_MSG_TO_CLIENT:
                bundle = new Bundle();
                bundle.putString("msg", clientMsg.getText().toString());
                message.setData(bundle);
                break;

        }

        try {
            serverMessenger.send(message);
        } catch (RemoteException re) {
            addLog("RemoteException : Sending message to service");
        }
    }

    //INCOMING REQUESTS

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BServerMessage.MSG_HANDSHAKE:
                    addLog("Handshake succcessful");
                    break;
                case BServerMessage.MSG_WAITING_FOR_CLIENT:
                    addLog("Waiting for client ...");
                    break;
                case BServerMessage.MSG_CLIENT_CONNECTED:
                    addLog("Client connected.Listening for messages...");
                    break;
                case BServerMessage.SERVER_SOCKET_CREATED:
                    addLog("BluetoothServerSocket created.");
                    break;
                case BServerMessage.MSG_FROM_CLIENT:
                    addLog("Client : " + msg.getData().getString("msg"));
                    break;
                case BServerMessage.MSG_CLIENT_COUNT:
                    addLog("Client : " + msg.getData().getInt("count"));
                    serverStatus.setText("Client Count : "+msg.getData().getInt("count"));
                    break;
                case BServerMessage.MSG_STOP_CONNECTION_THREAD:
                    addLog("Connection thread stopped.");
                    break;
                case BServerMessage.BROKEN_PIPE:
                    if(isBound) {
                        isBound = false;
                        stopServer.setEnabled(false);
                        startServer.setEnabled(true);
                        unbindService(MainActivity.this);
                        addLog("Client Disconnected..");
                    }
                    break;


                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
