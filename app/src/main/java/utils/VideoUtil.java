package utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import java.util.Formatter;
import java.util.Locale;

public class VideoUtil {
    private static final String PROGRESS_FILE = "VIDEO_PROGRESS";

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }

    public static Activity scanForActivity(Context context) {
        if (context == null)
            return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }


    public static void saveProgress(Context context, String url, long progress) {
        if (progress < 3000) {
            progress = 0;
        }
        context.getSharedPreferences(PROGRESS_FILE, Context.MODE_PRIVATE)
                .edit()
                .putLong("progress:" + url, progress)
                .apply();
    }

    public static long getSavedProgress(Context context, String url) {
        return context.getSharedPreferences(PROGRESS_FILE, Context.MODE_PRIVATE)
                .getLong("progress:" + url, 0);
    }

    public static void clearSavedProgress(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            context.getSharedPreferences(PROGRESS_FILE, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
        } else {
            context.getSharedPreferences(PROGRESS_FILE, Context.MODE_PRIVATE)
                    .edit()
                    .putLong("progress:" + url, 0)
                    .apply();
        }
    }

    /**
     * 将毫秒数格式化为"##:##"的时间
     *
     * @param milliseconds 毫秒数
     * @return ##:##
     */
    public static String formatTime(long milliseconds) {
        if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        long totalSeconds = milliseconds / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
