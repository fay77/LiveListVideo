package utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

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
}
