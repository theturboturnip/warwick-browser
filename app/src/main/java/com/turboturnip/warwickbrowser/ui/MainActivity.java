package com.turboturnip.warwickbrowser.ui;

import android.Manifest;
import androidx.lifecycle.Observer;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import com.turboturnip.warwickbrowser.SortBy;
import com.turboturnip.warwickbrowser.Statics;
import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleCreate;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleDelete;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleLinkInsert;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleUpdateDescription;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleUpdateSortBy;
import com.turboturnip.warwickbrowser.ui.dialog.AddModuleDialogFragment;
import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.db.ModuleAndLinks;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.ModuleLink;
import com.turboturnip.warwickbrowser.ui.dialog.DeleteModuleDialogFragment;
import com.turboturnip.warwickbrowser.ui.dialog.ModuleDialogHandler;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.turboturnip.warwickbrowser.ui.ModuleAddLinkActivity.MODULE_ID;

public class MainActivity extends AppCompatActivity implements AddModuleDialogFragment.AddModuleListener, ModuleDialogHandler {

    private RecyclerView moduleHolder;
    private ModuleViewAdapter moduleAdapter;
    private LinearLayoutManager moduleLayout;
    private View permissionsDialog;

    private RecyclerView.RecycledViewPool modulePool = new RecyclerView.RecycledViewPool(),
            moduleLinkPool = new RecyclerView.RecycledViewPool(),
            moduleFilePool = new RecyclerView.RecycledViewPool();

    private ModuleDatabase moduleDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moduleDatabase = ModuleDatabase.getDatabase(this);

        permissionsDialog = findViewById(R.id.permission_dialog);
        permissionsDialog.setVisibility(View.GONE);
        checkPermission();

        moduleAdapter = new ModuleViewAdapter(this, moduleLinkPool, moduleFilePool);
        moduleDatabase.daoModules().getModules().observe(this, moduleAdapter.moduleObserver);

        moduleLayout = new LinearLayoutManager(this);
        moduleLayout.setOrientation(RecyclerView.VERTICAL);

        moduleHolder = findViewById(R.id.modules);
        moduleHolder.setRecycledViewPool(modulePool);
        moduleHolder.setAdapter(moduleAdapter);
        moduleHolder.setLayoutManager(moduleLayout);
        moduleAdapter.notifyDataSetChanged();

        findViewById(R.id.my_warwick_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("uk.ac.warwick.my.app");
                if (intent != null)
                    startActivity(intent);
            }
        });
    }

    private static final int REQUEST_EXT_STORAGE = 0;
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                permissionsDialog.setVisibility(View.VISIBLE);
                permissionsDialog.findViewById(R.id.permission_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("turnipwarwick", "Requesting again");
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_EXT_STORAGE);
                    }
                });
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_EXT_STORAGE);
            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXT_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsDialog.setVisibility(View.GONE);
                } else {
                    permissionsDialog.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_module) {
            AddModuleDialogFragment newFragment = new AddModuleDialogFragment();
            newFragment.show(getSupportFragmentManager(), "addModule");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onModuleAdded(final String title) {
        new AsyncDBModuleCreate(moduleDatabase, title).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == 0 && data != null) {
            final long moduleId = data.getLongExtra(MODULE_ID, -1);
            final String linkName = data.getStringExtra("LINK_NAME");
            final String linkTarget = data.getStringExtra("LINK_TARGET");
            Log.e("turnipwarwick", "Got data back from link selection: " + moduleId + " : " + linkName + " : " + linkTarget);
            new AsyncDBModuleLinkInsert(moduleDatabase, new ModuleLink(moduleId, linkName, linkTarget)).execute();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDeleteRequestAccepted(long moduleID) {
        new AsyncDBModuleDelete(ModuleDatabase.getDatabase(this), moduleID).execute();
    }

    @Override
    public void onDescriptionUpdateRequested(long moduleId, String newDescription) {
        new AsyncDBModuleUpdateDescription(ModuleDatabase.getDatabase(this), moduleId, newDescription).execute();
    }

    @Override
    public void onSortUpdateRequested(long moduleId, SortBy newSort) {
        new AsyncDBModuleUpdateSortBy(ModuleDatabase.getDatabase(this), moduleId, newSort).execute();
    }
}
