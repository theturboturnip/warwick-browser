package com.turboturnip.warwickbrowser.db.actions;

import com.turboturnip.warwickbrowser.WarwickUrls;
import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.ModuleLink;

public class AsyncDBModuleCreate extends AsyncDBAction {
    private final String moduleName;

    public AsyncDBModuleCreate(ModuleDatabase moduleDatabase, String moduleName) {
        super(moduleDatabase);
        this.moduleName = moduleName;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        long moduleId = moduleDatabase.daoModules().insertModule(new Module(moduleName));
        if (moduleName.startsWith("CS")) {
            moduleDatabase.daoModules().insertModuleLink(new ModuleLink(moduleId, "Home", WarwickUrls.createWarwickUrl("fac/sci/dcs/teaching/material/" + moduleName + "/").toString()));
        } else if (moduleName.startsWith("ES")) {
            int year = Integer.parseInt("" + moduleName.charAt(2));
            if (year > 4)
                year = 4;
            moduleDatabase.daoModules().insertModuleLink(new ModuleLink(moduleId, "Home", WarwickUrls.createWarwickUrl("fac/sci/eng/eso/modules/year" + year + "/" + moduleName + "/").toString()));
        }
        moduleDatabase.daoModules().insertModuleLink(new ModuleLink(moduleId, "Exam Papers", WarwickUrls.createWarwickUrl("services/exampapers/", "q", moduleName).toString()));
        return null;
    }
}
