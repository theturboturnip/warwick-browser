package com.turboturnip.warwickbrowser.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.SortBy;
import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.ModuleAndLinks;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.ModuleLink;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleDelete;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleLinkInsert;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleUpdateDescription;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleUpdateSortBy;
import com.turboturnip.warwickbrowser.ui.dialog.DeleteModuleDialogFragment;
import com.turboturnip.warwickbrowser.ui.dialog.ModuleDialogHandler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.turboturnip.warwickbrowser.ui.ModuleAddLinkActivity.MODULE_ID;

public class SingleModuleActivity extends AppCompatActivity implements ModuleDialogHandler {
    ModuleView moduleView;

    public static final String ID_EXTRA = "id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_module);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        moduleView = new ModuleView(findViewById(R.id.module_layout), this, null, null, false);
        new UpdateModuleTask(ModuleDatabase.getDatabase(this), moduleView).execute(getIntent().getLongExtra(ID_EXTRA, -1));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private static class UpdateModuleTask extends AsyncTask<Long, Void, ModuleAndLinks> {
        private ModuleView v;
        private ModuleDatabase db;
        UpdateModuleTask(ModuleDatabase database, ModuleView v){
            this.v = v;
            db = database;
        }

        @Override
        protected ModuleAndLinks doInBackground(Long... integers) {
            if (integers.length == 0 || integers[0] < 0) return null;
            return db.daoModules().getModuleFromId(integers[0]);
        }

        @Override
        protected void onPostExecute(ModuleAndLinks moduleAndLinks) {
            if (moduleAndLinks != null)
                v.updateContents(moduleAndLinks);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == 0 && data != null) {
            final long moduleId = data.getLongExtra(MODULE_ID, -1);
            final String linkName = data.getStringExtra("LINK_NAME");
            final String linkTarget = data.getStringExtra("LINK_TARGET");
            Log.d("turnipwarwick", "Got data back from link selection: " + moduleId + " : " + linkName + " : " + linkTarget);
            new AsyncDBModuleLinkInsert(ModuleDatabase.getDatabase(this), new ModuleLink(moduleId, linkName, linkTarget)).execute();
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDeleteRequestAccepted(long moduleID) {
        new AsyncDBModuleDelete(ModuleDatabase.getDatabase(this), moduleID).execute();
        finish();
    }

    @Override
    public void onDescriptionUpdateRequested(long moduleId, String newDescription) {
        new AsyncDBModuleUpdateDescription(ModuleDatabase.getDatabase(this), moduleId, newDescription).execute();
        finish();
    }

    @Override
    public void onSortUpdateRequested(long moduleId, SortBy newSort) {
        new AsyncDBModuleUpdateSortBy(ModuleDatabase.getDatabase(this), moduleId, newSort).execute();
        finish();
    }
}
