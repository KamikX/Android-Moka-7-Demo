package sk.kamil.morley.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import sk.kamil.morley.plc.ConnectionParams;
import sk.kamil.morley.plc.RwParams;
import sk.kamil.morley.utils.NotificationUtils;

import timber.log.Timber;

public class PlcService extends Service {

    private static final String TAG = PlcService.class.getCanonicalName();
    private PlcServiceThread plcServiceThread;
    private PowerManager.WakeLock wakeLock;
    private final IBinder binder = new PlcBinder();


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class PlcBinder extends Binder {
        public PlcService getService() {
            return PlcService.this;
        }
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            Timber.d("Wake lock acquire...");
            this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            this.wakeLock.acquire();
        }
        this.plcServiceThread = new PlcServiceThread(getApplicationContext());
        startPlcServiceThread();
        showNotification();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();

    }

    /**
     * Stop service routines
     */
    private void stop() {

        if (this.wakeLock != null && this.wakeLock.isHeld()) {
            Timber.d("Wake lock release...");
            this.wakeLock.release();
        }
        Timber.i("Stop Foreground plc service");
        stopPlcServiceThread();
        stopForeground(true);
        stopSelf();
    }

    /**
     * Start plc service thread
     */
    private void startPlcServiceThread() {
        if (this.plcServiceThread != null) {
            Timber.d("Plc service running...");
            this.plcServiceThread.start();
            this.plcServiceThread.prepareHandler();
        }

    }

    /**
     * Stop plc service thread
     */
    private void stopPlcServiceThread() {
        // stop service thread
        if (this.plcServiceThread != null) {
            Timber.d("Plc service stopping...");
            if (this.plcServiceThread.isAlive()) {
                this.plcServiceThread.stopService();
            }
        }
    }

    /**
     * Connect to plc
     */
    public void connectToPlc(ConnectionParams connectionParams) {
        if (this.plcServiceThread != null) {
            this.plcServiceThread.connect(connectionParams);
        }
    }

    /**
     * Disconnect from plc
     */
    public void disconnectFromPlc() {
        if (this.plcServiceThread != null) {
            this.plcServiceThread.disconnect();
        }
    }

    /**
     * Read from plc
     */
    public void read(RwParams rwParams) {
        if (this.plcServiceThread != null) {
            this.plcServiceThread.read(rwParams);
        }
    }

    /**
     * Write to plc
     */
    public void write(RwParams rwParams) {
        if (this.plcServiceThread != null) {
            this.plcServiceThread.write(rwParams);
        }
    }


    /**
     * Show foreground service notification
     */
    private void showNotification() {
        startForeground(NotificationUtils.SERVICE_NOTIFICATION_ID,
                NotificationUtils.createForegroundServiceNotification(this));
    }

}
