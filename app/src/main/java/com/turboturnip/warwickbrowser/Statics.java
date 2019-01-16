package com.turboturnip.warwickbrowser;

import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;

import java.io.File;
import java.util.regex.Pattern;

public class Statics {
    public static final Pattern MODULE_NAME_PATTERN = Pattern.compile("[A-Za-z]{2}[A-Za-z0-9]{3}");

    public static void cookieSetup() {
        // Required for downloading from Moodle
        CookieManager.setAcceptFileSchemeCookies(true);
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getStorageDirForModule(String module){
        if (!isExternalStorageWritable())
            return null;
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "WarwickBrowser" + "/" + module);
        if (!dir.mkdirs()){
            Log.e("turnipwarwick", "Didn't create directory");
        }
        return dir;
    }

    private Statics(){}
}
