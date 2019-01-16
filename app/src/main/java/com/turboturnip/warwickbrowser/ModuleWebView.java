package com.turboturnip.warwickbrowser;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class ModuleWebView extends AppCompatActivity implements RedownloadFileDialogFragment.ShouldDownloadListener {

    public final static String REQUESTED_PATH = "requested_url";
    public final static String MODULE_NAME = "module_name";
    private String moduleName;
    private DownloadManager downloadManager;
    WebView webView;

    private class DownloadRequest {
        DownloadManager.Request request;
        Uri destinationUri;

        DownloadRequest(DownloadManager.Request r, Uri uri) {
            this.request = r;
            this.destinationUri = uri;
        }
    }

    private SparseArray<DownloadRequest> requests = new SparseArray<>();
    private int nextRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Required for downloading from Moodle
        CookieManager.setAcceptFileSchemeCookies(true);
        if (Build.VERSION.SDK_INT >= 21) {
            // Third party cookies are needed for Warwick
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        setContentView(R.layout.activity_module_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        moduleName = getIntent().getExtras().getString(MODULE_NAME, "no-module");
        String targetPath = getIntent().getExtras().getString(REQUESTED_PATH, "about:blank");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(moduleName);
        }

        Uri targetUrl = Uri.parse(targetPath.startsWith("http") ? targetPath : "https://warwick.ac.uk/" + targetPath);
        Log.e("turnipwarwick", "URI: " + targetUrl);

        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);

        webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
                Uri requestURI = Uri.parse(url);
                CookieManager cookieManager = CookieManager.getInstance();
                String cookie = cookieManager.getCookie(url);
                URI destinationJDKUri = new File(DownloadHelper.getStorageDirForModule(moduleName), requestURI.getLastPathSegment()).toURI();
                Uri destinationUri = Uri.parse(destinationJDKUri.toString());
                String baseName = requestURI.getLastPathSegment();

                Log.e("turnipwarwick", "Requested URI: " + requestURI);
                Log.e("turnipwarwick", "Cookies: " + cookie);
                Log.e("turnipwarwick", "Downloading to " + destinationUri);

                DownloadManager.Request request = new DownloadManager.Request(requestURI)
                        .addRequestHeader("Cookie", cookie)
                        .setMimeType(mimetype)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationUri(destinationUri)
                        .setTitle(baseName)
                        .setDescription("Downloading File")
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true)
                        .setVisibleInDownloadsUi(true);

                tryDownload(new DownloadRequest(request, destinationUri));
            }
        });
        webView.loadUrl(targetUrl.toString());
    }

    private void tryDownload(DownloadRequest request) {
        if (new File(request.destinationUri.getPath()).exists()) {
            // Already exists, ask user if they want to download it again
            int id = nextRequestId++;
            requests.put(id, request);
            RedownloadFileDialogFragment newFragment = RedownloadFileDialogFragment.newInstance(id, request.destinationUri.getLastPathSegment(), moduleName);
            newFragment.show(getSupportFragmentManager(), "redownloadModule");
        } else {
            honorDownloadRequest(request);
        }
    }

    @Override
    public void onRedownloadRequested(int internalRequestId) {
        DownloadRequest request = requests.get(internalRequestId);
        if (request == null) return;
        File preexisting = new File(request.destinationUri.getPath());
        if (preexisting.exists())
            preexisting.delete();
        honorDownloadRequest(request);
    }

    private void honorDownloadRequest(DownloadRequest request) {
        downloadManager.enqueue(request.request);
        Snackbar.make(webView, "Downloading " + request.destinationUri.getLastPathSegment(), Snackbar.LENGTH_LONG).show();
    }
}
