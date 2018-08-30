package sk.kamil.morley.utils.logger;

import android.util.Log;

import timber.log.Timber;

/**
 * Created by Kamil on 9/19/2017.
 */

public class ReleaseTree extends Timber.Tree {

    private static final int MAX_LOG_LENGTH = 4000;

    @Override
    protected boolean isLoggable(String tag, int priority) {

        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return false;
        }

        // only log WARN, ERROR
        return true;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {

        if (message.length() < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(tag, message);
            } else {
                Log.println(priority, tag, message);
            }
            return;
        }

        for (int i = 0, length = message.length(); i < length; i++) {
            int newLine = message.indexOf('\n', i);
            newLine = newLine != -1 ? newLine : length;

            do {
                int end = Math.min(newLine, i + MAX_LOG_LENGTH);
                String part = message.substring(i, end);
                if (priority == Log.ASSERT) {
                    Log.wtf(tag, message);
                } else {
                    Log.println(priority, tag, part);
                }
                i = end;
            } while (i < newLine);
        }

    }
}
