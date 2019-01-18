package com.turboturnip.warwickbrowser.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class ModuleLink {
    @PrimaryKey(autoGenerate = true)
    public final Long id;

    public final long parentModuleId;

    public final String title;
    public final String target;

    @Ignore
    public ModuleLink(long parentModuleId, String title, String target) {
        this(null, parentModuleId, title, target);
    }
    public ModuleLink(Long id, long parentModuleId, String title, String target) {
        this.id = id;
        this.parentModuleId = parentModuleId;
        this.title = title;
        this.target = target;
    }
}
