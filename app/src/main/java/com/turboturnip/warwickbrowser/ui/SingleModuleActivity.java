package com.turboturnip.warwickbrowser.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.ModuleAndLinks;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleDelete;
import com.turboturnip.warwickbrowser.ui.dialog.DeleteModuleDialogFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SingleModuleActivity extends AppCompatActivity implements DeleteModuleDialogFragment.ShouldDeleteListener {
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
    public void onDeleteRequestAccepted(long moduleID) {
        new AsyncDBModuleDelete(ModuleDatabase.getDatabase(this), moduleID).execute();
        finish();
    }
}
