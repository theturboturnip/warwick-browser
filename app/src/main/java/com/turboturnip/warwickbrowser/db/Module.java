package com.turboturnip.warwickbrowser.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

@Entity
public class Module {
    @PrimaryKey(autoGenerate = true)
    public final Long id;
    public final String title;

    @Ignore
    public Module(String title){
        this(null, title);
    }
    public Module(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}
