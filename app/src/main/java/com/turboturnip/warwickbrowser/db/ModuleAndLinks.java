package com.turboturnip.warwickbrowser.db;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ModuleAndLinks {
    @Embedded
    public Module module;
    @Relation(parentColumn = "id", entityColumn = "parentModuleId")
    public List<ModuleLink> links;
}
