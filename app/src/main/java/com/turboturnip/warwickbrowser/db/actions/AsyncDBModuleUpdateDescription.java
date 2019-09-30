package com.turboturnip.warwickbrowser.db.actions;

import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.ModuleLink;

public class AsyncDBModuleUpdateDescription extends AsyncDBAction {
    private final long moduleId;
    private final String newDescription;

    public AsyncDBModuleUpdateDescription(ModuleDatabase moduleDatabase, long moduleId, String newDescription) {
        super(moduleDatabase);
        this.moduleId = moduleId;
        this.newDescription = newDescription;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        moduleDatabase.daoModules().updateDescription(moduleId, newDescription);
        return null;
    }
}
