package com.turboturnip.warwickbrowser.db.actions;

import com.turboturnip.warwickbrowser.SortBy;
import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;

public class AsyncDBModuleUpdateSortBy extends AsyncDBAction {
    private final long moduleId;
    private final SortBy newSort;

    public AsyncDBModuleUpdateSortBy(ModuleDatabase moduleDatabase, long moduleId, SortBy newSort) {
        super(moduleDatabase);
        this.moduleId = moduleId;
        this.newSort = newSort;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        moduleDatabase.daoModules().updateSortBy(moduleId, newSort);
        return null;
    }
}
