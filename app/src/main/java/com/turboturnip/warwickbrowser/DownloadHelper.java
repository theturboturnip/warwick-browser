package com.turboturnip.warwickbrowser;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class DownloadHelper {
    private DownloadHelper(){}

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
}
