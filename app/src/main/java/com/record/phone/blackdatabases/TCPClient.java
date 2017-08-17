package com.record.phone.blackdatabases;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by XSS on 01/08/2017.
 */

public class TCPClient {

    public static final String SERVER_IP = "182.180.51.219"; //server IP address
    //public static final String SERVER_IP = "192.168.10.30"; //server IP address
    public static final int SERVER_PORT = 36588;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        while (true) {

            mRun = true;

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                Socket socket = new Socket(serverAddr, SERVER_PORT);

                if (socket.isClosed()) {
                    socket.connect(new InetSocketAddress(InetAddress.getByName(SERVER_IP), SERVER_PORT));
                } else {
                    try {
                        mBufferOut = new PrintWriter(socket.getOutputStream());
                        mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        int charsRead = 0;
                        char[] buffer = new char[1024]; //choose your buffer size if you need other than 1024

                        while (mRun) {
                            charsRead = mBufferIn.read(buffer);
                            mServerMessage = new String(buffer).substring(0, charsRead);
                            if (mServerMessage != null && mMessageListener != null) {
                                mMessageListener.messageReceived(mServerMessage);
                            }
                            mServerMessage = null;
                        }

                    } catch (Exception e) {
                        mMessageListener.messageReceived("Connection timeout");
                    } finally {
                        //the socket must be closed. It is not possible to reconnect to this socket
                        // after it is closed, which means a new socket instance has to be created.
                        mMessageListener.messageReceived("Connection reset");
                        socket.close();
                    }
                }

            } catch (Exception e) {

            }

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}