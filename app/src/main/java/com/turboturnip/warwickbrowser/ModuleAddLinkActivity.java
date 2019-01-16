package com.turboturnip.warwickbrowser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static com.turboturnip.warwickbrowser.ModuleWebView.MODULE_NAME;

public class ModuleAddLinkActivity extends AppCompatActivity implements AddModuleLinkDialogFragment.AddModuleLinkListener {
    public static final String MODULE_ID = "module-id";

    private long moduleId;
    private WebView webView;
    private String moduleName;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select:
                AddModuleLinkDialogFragment newFragment = new AddModuleLinkDialogFragment();
                newFragment.show(getSupportFragmentManager(), "addModuleLink");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        moduleId = getIntent().getExtras().getLong(MODULE_ID, -1);
        moduleName = getIntent().getExtras().getString(MODULE_NAME, "no-module");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Adding link for " + moduleName);
        }

        webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        webView.loadUrl("https://search.warwick.ac.uk/website?q="+moduleName);
    }

    @Override
    public String getLinkTarget() {
        return webView.getUrl();
    }

    @Override
    public void onModuleLinkAdded(String title, String path) {
        Intent data = new Intent();
        data.putExtra(MODULE_ID, moduleId);
        data.putExtra("LINK_NAME", title);
        data.putExtra("LINK_TARGET", path);//.new Uri.Builder().appendQueryParameter(MODULE_ID, ""+moduleId).appendQueryParameter("LINK_TARGET", path).build());
        setResult(0, data);
        finish();
    }
}
