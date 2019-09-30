package com.turboturnip.warwickbrowser.db;

import com.turboturnip.warwickbrowser.SortBy;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;
import androidx.room.TypeConverters;

@Entity
public class Module {
    @PrimaryKey(autoGenerate = true)
    public final Long id;
    public final String title;
    public final String description;
    @TypeConverters(SortBy.class)
    public final SortBy sortBy;

    @Ignore
    public Module(String title){
        this(null, title, "", SortBy.Default);
    }
    public Module(Long id, String title, String description, SortBy sortBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.sortBy = sortBy;
    }
}
