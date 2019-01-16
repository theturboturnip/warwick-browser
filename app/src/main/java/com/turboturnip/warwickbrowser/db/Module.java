package com.turboturnip.warwickbrowser.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Relation;

import java.util.ArrayList;
import java.util.List;

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
