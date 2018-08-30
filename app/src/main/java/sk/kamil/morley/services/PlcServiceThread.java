package sk.kamil.morley.services;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;

import sk.kamil.morley.plc.ConnectionParams;
import sk.kamil.morley.plc.RwParams;
import sk.kamil.morley.plc.ReadResult;
import sk.kamil.morley.plc.SimplePlc;

import timber.log.Timber;

public class PlcServiceThread extends HandlerThread {


    public static String PLC_SERVICE_ACTION = "PLC_SERVICE_ACTION";
    private static final int STOP = 0;

    /**
     * Actions
     */
    public static final int CONNECT = 2;
    public static final int DISCONNECT = 3;
    public static final int WRITE = 4;
    public static final int READ = 5;


    /**
     * Callback messages
     */

    public static final String PLC_SERVICE_CALLBACK = "PLC_SERVICE_CALLBACK";
    public static final String PLC_SERVICE_CALLBACK_ERROR = "PLC_SERVICE_CALLBACK_ERROR";
    public static final int CONNECT_SUCCESS = 10;
    public static final int CONNECT_FAULT = 11;
    public static final int READ_SUCCESS = 12;
    public static final int READ_FAULT = 13;
    public static final int WRITE_SUCCESS = 14;
    public static final int WRITE_FAULT = 15;



    public static final String READ_VALUE = "READ_VALUE";



    private Context context;
    private Handler workerHandler;
    private SimplePlc simplePlc;
    private LocalBroadcastManager localBroadcastManager;


    public PlcServiceThread(Context context) {
        super("PlcServiceThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        this.context = context;
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.simplePlc = new SimplePlc();
    }


    void prepareHandler() {
        this.workerHandler = new Handler(getLooper(), msg -> {
            switch (msg.what) {
                case CONNECT:

                    onConnect((ConnectionParams) msg.obj);
                    break;

                case DISCONNECT:
                    onDisconnect();
                    break;

                case READ:
                    onRead((RwParams) msg.obj);
                    break;

                case WRITE:
                    onWrite((RwParams) msg.obj);
                    break;

                case STOP:
                    onStop();
                    break;
            }
            return true;
        });
    }

    /**
     * Stop plc service
     */
    void stopService() {
        this.workerHandler.obtainMessage(STOP).sendToTarget();
    }

    /**
     * Connect to plc
     */
    public void connect(ConnectionParams connectionParams) {
        this.workerHandler.obtainMessage(CONNECT, connectionParams).sendToTarget();
    }

    /**
     * Disconnect from plc
     */
    public void disconnect() {
        this.workerHandler.obtainMessage(DISCONNECT).sendToTarget();
    }


    /**
     * Read from plc
     */
    public void read(RwParams rwParams) {
        this.workerHandler.obtainMessage(READ, rwParams).sendToTarget();
    }

    /**
     * Write to plc
     */
    public void write(RwParams rwParams) {
        this.workerHandler.obtainMessage(WRITE, rwParams).sendToTarget();
    }



    /**
     * Called when service connecting to plc
     */
    private void onConnect(ConnectionParams connectionParams) {
        Timber.i("Connect (Plc Service thread)");
        if (this.simplePlc != null) {
            Intent msg = new Intent(PLC_SERVICE_ACTION);

            String resultMessage = this.simplePlc.connect(connectionParams);

            // If result message in null, everything is ok
            if (resultMessage == null) {
                msg.putExtra(PLC_SERVICE_CALLBACK, CONNECT_SUCCESS);

            } else { // Otherwise contain error message
                msg.putExtra(PLC_SERVICE_CALLBACK, CONNECT_FAULT);
                msg.putExtra(PLC_SERVICE_CALLBACK_ERROR, resultMessage);
            }

            sendMessageToUi(msg);
        }
    }

    /**
     * Called when service disconnecting form plc
     */
    private void onDisconnect() {
        Timber.i("Disconnect (Plc Service thread)");
        if (this.simplePlc != null) {
            simplePlc.disconnect();
        }
    }

    /**
     * Called when service read form plc
     */
    private void onRead(RwParams rwParams) {
        Timber.i("Read (Plc Service thread)");
        if (this.simplePlc != null) {
            Intent msg = new Intent(PLC_SERVICE_ACTION);
            ReadResult result = simplePlc.read(rwParams);
            Timber.d("Read result: %s", result.getFormattedResult());

            if (result.getError() != null) {
                msg.putExtra(PLC_SERVICE_CALLBACK,READ_FAULT);
                msg.putExtra(PLC_SERVICE_CALLBACK_ERROR, result.getError());
            } else {
                msg.putExtra(PLC_SERVICE_CALLBACK,READ_SUCCESS);
                msg.putExtra(READ_VALUE, result.getFormattedResult());
            }
            sendMessageToUi(msg);
        }
    }


    /**
     * Called when service write to plc
     */
    private void onWrite(RwParams rwParams) {
        Timber.i("Write (Plc Service thread)");
        if (this.simplePlc != null) {
            Intent msg = new Intent(PLC_SERVICE_ACTION);

            String writeResult = simplePlc.write(rwParams);
            Timber.d("Write result: %s", writeResult);

            if (writeResult == null) {
                msg.putExtra(PLC_SERVICE_CALLBACK,WRITE_SUCCESS);
            } else {
                msg.putExtra(PLC_SERVICE_CALLBACK,WRITE_FAULT);
                msg.putExtra(PLC_SERVICE_CALLBACK_ERROR, writeResult);
            }
            sendMessageToUi(msg);
        }
    }



    /**
     * Called when service stop
     */
    private void onStop() {
        // destroy handler thread
        quit();
    }

    private void sendMessageToUi(Intent msg) {
        this.localBroadcastManager.sendBroadcast(msg);
    }


}
