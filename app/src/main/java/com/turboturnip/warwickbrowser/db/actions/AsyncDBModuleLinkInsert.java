package com.turboturnip.warwickbrowser.db.actions;

import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.ModuleLink;

public class AsyncDBModuleLinkInsert extends AsyncDBAction {
    private final ModuleLink toInsert;

    public AsyncDBModuleLinkInsert(ModuleDatabase moduleDatabase, ModuleLink toInsert) {
        super(moduleDatabase);
        this.toInsert = toInsert;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        moduleDatabase.daoModules().insertModuleLink(toInsert);
        return null;
    }
}
