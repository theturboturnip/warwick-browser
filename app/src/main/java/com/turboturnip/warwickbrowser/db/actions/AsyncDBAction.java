package com.turboturnip.warwickbrowser.db.actions;

import android.os.AsyncTask;

import com.turboturnip.warwickbrowser.db.ModuleDatabase;

abstract class AsyncDBAction extends AsyncTask<Void, Void, Void> {
    protected final ModuleDatabase moduleDatabase;

    protected AsyncDBAction(ModuleDatabase moduleDatabase) {
        this.moduleDatabase = moduleDatabase;
    }
}
