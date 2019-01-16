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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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

public class ModuleWebView extends AppCompatActivity {

    public final static String REQUESTED_PATH = "requested_url";
    public final static String MODULE_NAME = "module_name";
    private final static String INTERCEPT_HEADER = "turnip-intercept://";
    private String targetPath;
    private String moduleName;
    private DownloadManager downloadManager;
    WebView webView;

    interface RedirectCallback {
        void onResolveRedirect(String newUrl);
    }
    private URL followRedirect(URL url) {
        URL toFollow = null;
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setInstanceFollowRedirects(true);
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(url.getHost());
            Log.e("turnipwarwick", "Cookies from " + url.getHost() + ": " + cookie);
            connection.addRequestProperty("Cookie", cookie);
            connection.connect();
            Log.e("turnipwarwick", "Resolving redirect: got response " + connection.getResponseCode());
            if (connection.getResponseCode() / 100 == 3) {
                toFollow = (new URL(connection.getHeaderField("Location")));
            }

            StringBuilder sb = new StringBuilder();
            InputStream isw = connection.getInputStream();
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                sb.append(current);
            }
            toFollow = connection.getURL();
            Log.e("turnipwarwick", "Response: " + sb.toString());
            connection.disconnect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (toFollow == null)
            return url;
        return toFollow;//followRedirect(toFollow);
    }
    private void followRedirectAsync(final String urlStr, final RedirectCallback callback) {
        final URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.onResolveRedirect(urlStr);
            return;
        }
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                callback.onResolveRedirect(followRedirect(url).toString());
                return null;
            }
        }.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieManager.setAcceptFileSchemeCookies(true);

        setContentView(R.layout.activity_module_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        moduleName = getIntent().getExtras().getString(MODULE_NAME, "no-module");
        targetPath = getIntent().getExtras().getString(REQUESTED_PATH, "about:blank");

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
                /*followRedirectAsync(url, new RedirectCallback() {
                    @Override
                    public void onResolveRedirect(String newUrl) {*/
                        Log.e("turnipwarwick", "Wants download from " + url);
                        Uri requestURI = Uri.parse(url);
                        Log.e("turnipwarwick", "Requested URI: " + requestURI);
                        DownloadManager.Request request = new DownloadManager.Request(requestURI);
                        CookieManager cookieManager = CookieManager.getInstance();
                        String cookie = cookieManager.getCookie(requestURI.toString());
                        Log.e("turnipwarwick", "Cookies from " + requestURI.getHost() + ": " + cookie);
                        request.addRequestHeader("Cookie", cookie);
                        request.setMimeType(mimetype);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                        final boolean useCheckedDestination = true;
                        if (useCheckedDestination) {
                            URI destinationJDKUri = new File(DownloadHelper.getStorageDirForModule(moduleName), requestURI.getLastPathSegment()).toURI();
                            Uri destinationUri = Uri.parse(destinationJDKUri.toString());
                            Log.e("turnipwarwick", "Downloading to " + destinationUri);
                            request.setDestinationUri(destinationUri);
                        } else {
                            // TODO: This doesn't work?
                            request.setDestinationInExternalFilesDir(ModuleWebView.this, Environment.DIRECTORY_DOCUMENTS, "WarwickBrowser" + "/" + moduleName + "/" + requestURI.getLastPathSegment());
                        }

                        request.setTitle(requestURI.getLastPathSegment());
                        request.setDescription("Downloading File");
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                                .setAllowedOverMetered(true)
                                .setAllowedOverRoaming(true)
                                .setVisibleInDownloadsUi(true);

                        long downloadID = downloadManager.enqueue(request);
                        Cursor cur = downloadManager.query(new DownloadManager.Query().setFilterById(downloadID));
                        if (cur.moveToFirst()) {
                            Log.e("turnipwarwick", "Status after start was " + cur.getInt(cur.getColumnIndex(DownloadManager.COLUMN_STATUS)));
                        }
                        cur.close();
                    /*}
                });*/


            }
        });
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri requestURI = request.getUrl();
                Log.e("turnipwarwick", "Loading URL " + request.getUrl());
                if (false && requestURI.getHost().equals("moodle.warwick.ac.uk") && requestURI.getPathSegments().size() > 0 && requestURI.getPathSegments().get(0).equals("pluginfile.php")) {
                    Log.e("turnipwarwick", "Found Moodle download at " + request.getUrl() + ", moving to intercept");

                    boolean shouldInstantRedir = true;
                    if (shouldInstantRedir) {
                        instantRedirect(request);
                    } else {
                        webView.loadUrl(encodeIntercept(requestURI));//INTERCEPT_HEADER + requestURI.toString());
                    }
                    return true;
                }
                //Log.e("turnipwarwick", "Trying to " + request.getMethod() + " URL " + request.getUrl());
                //if (request.getUrl().getLastPathSegment().contains(".") && !request.getUrl().getLastPathSegment().endsWith(".html"))
                //    return false;
                //if (request.getUrl().getPath().startsWith("/" + targetPath))
                return false;
                //if (request.getUrl().getHost().equals("websignon.warwick.ac.uk"))
                //    return false;
                //Log.e("turnipwarwick", "Blocked request for " + request.getUrl().getPath() + " with host " + request.getUrl().getHost());
                //return true;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri requestURI = request.getUrl();
                Log.e("turnipwarwick", "TestIntercept for  URL " + request.getUrl());
                if (isIntercept(requestURI)){//requestURI.toString().startsWith(INTERCEPT_HEADER)) {
                    Log.e("turnipwarwick", "Trying to intercept " + request.getMethod() + " of " + request.getUrl());
                    return getURL(requestURI, request.getRequestHeaders());
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        webView.loadUrl(targetUrl.toString());
        if (Build.VERSION.SDK_INT >= 21) {
            // AppRTC requires third party cookies to work
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }
    }

    // ISSUE: The host isn't included in the links in the file, so if it's captured from turnip-warwick-intercept.doesntexist, all subsequent ones will attempt to be found from the same place
    private static final String INTERCEPT_WEBSITE = "turnip-warwick-intercept.doesntexist";
    private static final String INTERCEPT_EXT = "exact-intercept.html";
    private static final String INTERCEPT_QUERY = "TO_INTERCEPT";
    private String encodeIntercept(Uri uri) {
        Uri.Builder b = new Uri.Builder();
        b.scheme(uri.getScheme()).authority(uri.getHost()).appendPath(INTERCEPT_WEBSITE);
        for (String pathBit : uri.getPathSegments()) {
            b.appendPath(pathBit);
        }
        b.encodedQuery(uri.getQuery());
        String endResult = b.build().toString();

        Log.e("turnipwarwick", "URL Intercept: " + uri.toString() + " to " + endResult);
        //b.appendQueryParameter(INTERCEPT_QUERY, Uri.encode(uri.toString()));
        return endResult;//Uri.fromParts(uri.getScheme(), INTERCEPT_WEBSITE + INTERCEPT_EXT, Uri.encode(uri.toString())).toString();
    }
    /*private boolean isExactIntercept(Uri url) {
        return isIntercept(url);//isIntercept(url) && url.getPathSegments().get(1).equals(INTERCEPT_WEBSITE);
    }*/
    private boolean isIntercept(Uri url) {
        if (url.getPathSegments().size() == 0)
            return false;
        return url.getPathSegments().get(0).equals(INTERCEPT_WEBSITE);
    }
    private String decodeExactIntercept(Uri interceptUrl) {
        return interceptUrl.toString().replace("/" + INTERCEPT_WEBSITE, "");
        //return Uri.decode(interceptUrl.getQueryParameter(INTERCEPT_QUERY));
    }
    /*private String decodeInexactIntercept(Uri interceptUrl) {
        return interceptUrl.toString().replace(INTERCEPT_WEBSITE + ".", "");
    }*/

    private WebResourceResponse getURL(Uri interceptURL, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(decodeExactIntercept(interceptURL));/*isExactIntercept(interceptURL) ? new URL(decodeExactIntercept(interceptURL)) : new URL(decodeInexactIntercept(interceptURL))*///interceptURL.substring(INTERCEPT_HEADER.length()));
            Log.e("turnipwarwick", "Manual GET for " + url);
            connection = (HttpURLConnection)url.openConnection();
            connection.setInstanceFollowRedirects(true);

            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie("warwick.ac.uk");//url.getHost());
            Log.e("turnipwarwick", "Cookies from " + url.getHost() + ": " + cookie);
            connection.addRequestProperty("Cookie", cookie);
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.addRequestProperty(header.getKey(), header.getValue());
                    Log.e("turnipwarwick", "Request Prop: " + header.getKey() + " : " + header.getValue());
                }
            }
            connection.addRequestProperty("Connection", "keep-alive");
            connection.addRequestProperty("Upgrade-Insecure-Requests", "1");

            connection.connect();

            String contentType = connection.getContentType();
            String[] contentTypeSplit = contentType.split(";");
            Log.e("turnipwarwick", "Intercept Data: " + contentTypeSplit[0] + " : " + connection.getContentEncoding());
            WebResourceResponse response = new WebResourceResponse(contentTypeSplit[0], connection.getContentEncoding(), connection.getInputStream());
            //response.setResponseHeaders(connection.get);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }
    private void instantRedirect(final WebResourceRequest request) {
        new AsyncTask<Void, Void, Pair<String, WebResourceResponse>>() {
            @Override
            protected Pair<String, WebResourceResponse> doInBackground(Void... voids) {
                WebResourceResponse response = getURL(Uri.parse(encodeIntercept(request.getUrl())), request.getRequestHeaders());
                if (response == null) return null;
                java.util.Scanner s = new java.util.Scanner(response.getData()).useDelimiter("\\A");
                return new Pair<>(s.hasNext() ? s.next() : "", response);
            }

            @Override
            protected void onPostExecute(Pair<String, WebResourceResponse> output) {
                String data = output.first;
                WebResourceResponse response = output.second;
                webView.loadDataWithBaseURL(request.getUrl().getHost(), data, response.getMimeType(), response.getEncoding(), request.getUrl().toString());
            }
        }.execute();

    }
}
