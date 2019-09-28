package com.turboturnip.warwickbrowser.db.actions;

import com.turboturnip.warwickbrowser.db.DaoModules;
import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;

public class AsyncDBModuleDelete extends AsyncDBAction {
    private final long moduleID;

    public AsyncDBModuleDelete(ModuleDatabase moduleDatabase, long moduleID) {
        super(moduleDatabase);
        this.moduleID = moduleID;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DaoModules dao = moduleDatabase.daoModules();
        dao.deleteModuleFromId(moduleID);
        return null;
    }
}
