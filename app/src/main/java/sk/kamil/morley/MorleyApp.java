package sk.kamil.morley;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import sk.kamil.morley.services.PlcService;
import sk.kamil.morley.utils.NotificationUtils;
import sk.kamil.morley.utils.logger.DebugTree;
import sk.kamil.morley.utils.logger.ReleaseTree;
import timber.log.Timber;

public class MorleyApp extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize library for logging
        initLogger();

        // Init PLC service
        initService();

    }

    /**
     * Init logger
     */
    public void initLogger() {

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }

    }

    /**
     * Init service
     */
    public void initService(){
        Timber.i("Initializing plc service..");
        Intent service = new Intent(this, PlcService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create notification channel service
            NotificationUtils.createNotificationChannels(this);
            // start foreground service
            startForegroundService(service);
        } else {
            startService(service);
        }
    }


}
