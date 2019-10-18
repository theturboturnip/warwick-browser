package com.turboturnip.warwickbrowser;

import android.net.Uri;

public class WarwickUrls {
    private WarwickUrls(){}

    /*public static Uri createWarwickUrl(String path) {
        return createWarwickUrl(path);
    }*/
    public static Uri createWarwickUrl(String path, String... queryBits) {
        Uri.Builder b =  new Uri.Builder().scheme("warwick").path(path);
        for (int i = 0; i < queryBits.length; i += 2) {
            b.appendQueryParameter(queryBits[i], queryBits[i+1]);
        }
        return b.build();
    }

    public static Uri resolveUrl(String baseUrl) {
        Uri asUri = Uri.parse(baseUrl);
        Uri.Builder b = asUri.buildUpon();
        //if (asUri.getScheme() == null || asUri.getScheme().equalsIgnoreCase("http"))
        b.scheme("https");
        if ((asUri.getScheme() != null && asUri.getScheme().equalsIgnoreCase("warwick")) || asUri.getHost() == null || !asUri.getHost().contains("."))
            b.path("warwick.ac.uk/" + asUri.getPath());
        return b.build();
    }
}
