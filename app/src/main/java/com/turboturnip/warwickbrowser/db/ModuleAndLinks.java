package com.turboturnip.warwickbrowser.db;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class ModuleAndLinks {
    @Embedded
    public Module module;
    @Relation(parentColumn = "id", entityColumn = "parentModuleId")
    public List<ModuleLink> links;
}
