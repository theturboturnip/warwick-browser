package com.turboturnip.warwickbrowser.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.turboturnip.warwickbrowser.ui.dialog.DownloadAsDialogFragment;
import com.turboturnip.warwickbrowser.ui.dialog.RedownloadFileDialogFragment;
import com.turboturnip.warwickbrowser.Statics;

import java.io.File;
import java.net.URI;
import java.util.List;

public class ModuleViewActivity extends WebViewActivity implements RedownloadFileDialogFragment.ShouldRedownloadListener, DownloadAsDialogFragment.DownloadAsListener {

    public final static String REQUESTED_PATH = "requested_url";
    public final static String MODULE_NAME = "module_name";
    private String moduleName;
    private DownloadManager downloadManager;



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

        if (getIntent().getExtras() == null) {
            Log.e("turnipwarwick", "ModuleViewActivity created without extras");
            finish();
            return;
        }

        moduleName = getIntent().getExtras().getString(MODULE_NAME, "no-module");
        String targetPath = getIntent().getExtras().getString(REQUESTED_PATH, "about:blank");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(moduleName);
        }

        Uri targetUrl = Uri.parse(targetPath.startsWith("http") ? targetPath : "https://warwick.ac.uk/" + targetPath);
        Log.e("turnipwarwick", "URI: " + targetUrl);

        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (webView.getHitTestResult().getType() != WebView.HitTestResult.SRC_ANCHOR_TYPE)
                    return true;
                String url = webView.getHitTestResult().getExtra();
                if (url == null)
                    return true;
                String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                String mimetype = "";
                if (extension != null) {
                    mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }
                Log.d("turnipwarwick", "Lonk Click: " + url);
                getDownloadRequest(new ValueCallback<DownloadRequest>() {
                    @Override
                    public void onReceiveValue(DownloadRequest value) {
                        tryDownload(value, true);
                    }
                }, url, mimetype);
                webView.setLongClickable(true);
                return true;
            }
        });

        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
                getDownloadRequest(new ValueCallback<DownloadRequest>() {
                    @Override
                    public void onReceiveValue(DownloadRequest value) {
                        tryDownload(value);
                    }
                }, url, mimetype);
            }
        });
        webView.loadUrl(targetUrl.toString());
    }

    private void getDownloadRequest(ValueCallback<DownloadRequest> requestValueCallback, final String url, final String mimetype) {
        final Uri requestURI = Uri.parse(url);
        final String cookie = CookieManager.getInstance().getCookie(url);

        getFolderName(new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String subdirectory) {
                File storageDir = Statics.getStorageDirForModule(moduleName);
                if (subdirectory != null && !subdirectory.isEmpty()) {
                    storageDir = new File(storageDir, subdirectory);
                    if (!storageDir.isDirectory())
                        storageDir.mkdirs();
                }
                URI destinationJDKUri = new File(storageDir, requestURI.getLastPathSegment()).toURI();
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

                requestValueCallback.onReceiveValue(new DownloadRequest(request, destinationUri));
            }
        });
    }

    private void tryDownload(DownloadRequest request) {
        tryDownload(request, false);
    }
    private void tryDownload(DownloadRequest request, boolean shouldRename) {
        if (shouldRename || new File(request.destinationUri.getPath()).exists()) {
            // Already exists, ask user if they want to download it again
            int id = nextRequestId++;
            requests.put(id, request);
            DownloadAsDialogFragment newFragment = DownloadAsDialogFragment.newInstance(id, request.destinationUri.getLastPathSegment(), moduleName);
            newFragment.show(getSupportFragmentManager(), "downloadAs");
        } else {
            honorDownloadRequest(request);
        }
    }

    @Override
    public void onModifiedDownloadRequested(int internalRequestId, String newFileName) {
        DownloadRequest request = requests.get(internalRequestId);
        if (request == null) return;
        String parentPath = new File(request.destinationUri.getPath()).getParentFile().getPath();
        request.destinationUri = request.destinationUri.buildUpon().path(parentPath).appendPath(newFileName).build();
        request.request.setDestinationUri(request.destinationUri);
        File preexisting = new File(request.destinationUri.getPath());
        if (preexisting.exists())
            preexisting.delete();

        honorDownloadRequest(request);
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

    private void getFolderName(ValueCallback<String> callback) {
        Uri currentUri = Uri.parse(webView.getUrl());
        Log.e("turnipwarwick", currentUri.toString());
        if (currentUri.getHost().equals("moodle.warwick.ac.uk")
                && currentUri.getPath().startsWith("/mod/folder/view.php")) {
            Log.e("turnipwarwick", "Fits folder definition");
            extractMoodleFolderName(callback);
        } else
            callback.onReceiveValue("");
    }
    private static final String MOODLE_EXTRACT_FOLDER_JS = "document.querySelector(\"[role=navigation] > .breadcrumb > .breadcrumb-item > a[title=Folder]\").childNodes[0].data";
    private void extractMoodleFolderName(ValueCallback<String> callback) {
        webView.evaluateJavascript(MOODLE_EXTRACT_FOLDER_JS, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                // Trim quote marks off the ends
                callback.onReceiveValue(value.substring(1, value.length() - 1));
            }
        });
    }
}
