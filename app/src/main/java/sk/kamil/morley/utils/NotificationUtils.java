package sk.kamil.morley.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.kamil.morley.R;
import timber.log.Timber;

/**
 * Utility class for creating and showing notification
 */

public class NotificationUtils {

    public static final int SERVICE_NOTIFICATION_ID = 88;
    private static final String INIT_CHANNEL_ID = "INIT_CHANNEL_ID";


    /**
     * Create notification channel for app
     *
     * @param context - Context
     */
    @TargetApi(Build.VERSION_CODES.O)
    public static void createNotificationChannels(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel initChannel = new NotificationChannel(INIT_CHANNEL_ID,
                context.getString(sk.kamil.morley.R.string.notification_all_init_channel_name), NotificationManager.IMPORTANCE_LOW);


        if (manager != null) {
            manager.createNotificationChannel(initChannel);
        } else {
            Timber.e("NotificationManager == null issue!!");
        }
    }


    /**
     * Create notification for foreground service
     *
     * @param context - Context
     * @return notification
     */
    public static Notification createForegroundServiceNotification(Context context) {
        Bitmap icon = NotificationUtils.drawableToBitmap(context.getDrawable(sk.kamil.morley.R.drawable.ic_android));
        return new NotificationCompat.Builder(context, INIT_CHANNEL_ID)
                .setContentTitle(context.getString(sk.kamil.morley.R.string.notification_all_title))
                .setTicker(context.getString(sk.kamil.morley.R.string.notification_all_title))
                .setContentText(context.getString(sk.kamil.morley.R.string.notification_all_service_started))
                .setSmallIcon(sk.kamil.morley.R.drawable.ic_android)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(true).build();
    }


    /**
     * Convert drawable to bitmap
     *
     * @param drawable - drawable
     * @return bitmap
     */
    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
