package com.example.hrawat.bserver;

/**
 * Created by hrawat on 26-04-2017.
 */

public class BServerMessage {

    static final int MSG_HANDSHAKE = 1;
    static final int MSG_WAITING_FOR_CLIENT = 2;
    static final int MSG_CLIENT_CONNECTED = 3;
    static final int SERVER_SOCKET_CREATED = 4;
    static final int MSG_HANDSHAKE_SUCCESS = 5;
    static final int MSG_UNBIND = 6;
    static final int MSG_CLIENT_COUNT = 7;
    static final int MSG_STOP_CONNECTION_THREAD = 8;

    static final int MSG_FROM_CLIENT = 66;
    static final int SEND_MSG_TO_CLIENT = 67;
    static final int MSG_SENT_TO_CLIENT = 68;

    static final int BROKEN_PIPE = 111;

}
