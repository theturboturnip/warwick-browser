package com.turboturnip.warwickbrowser.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.turboturnip.warwickbrowser.ui.dialog.AddModuleLinkDialogFragment;
import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.Statics;

import static com.turboturnip.warwickbrowser.ui.ModuleViewActivity.MODULE_NAME;

public class ModuleAddLinkActivity extends WebViewActivity implements AddModuleLinkDialogFragment.AddModuleLinkListener {
    public static final String MODULE_ID = "module-id";

    private long moduleId;
    private String moduleName;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!super.onCreateOptionsMenu(menu))
            return false;
        getMenuInflater().inflate(R.menu.add_module_link_menu, menu);

        return true;
    }

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() == null) {
            Log.e("turnipwarwick", "ModuleAddLinkActivity created without extras");
            finish();
            return;
        }

        moduleId = getIntent().getExtras().getLong(MODULE_ID, -1);
        moduleName = getIntent().getExtras().getString(MODULE_NAME, "no-module");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Adding link for " + moduleName);
        }

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Snackbar.make(webView, "Can't download files in this mode.", Snackbar.LENGTH_SHORT).show();
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
        data.putExtra("LINK_TARGET", path);
        setResult(0, data);
        finish();
    }
}
