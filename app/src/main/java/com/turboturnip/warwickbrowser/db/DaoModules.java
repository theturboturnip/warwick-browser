package com.turboturnip.warwickbrowser.db;

import com.turboturnip.warwickbrowser.SortBy;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.TypeConverters;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DaoModules {
    @Insert
    long insertModule(Module module);
    @Insert
    long insertModuleLink(ModuleLink link);

    @Transaction
    @Query("SELECT * FROM Module")
    LiveData<List<ModuleAndLinks>> getModules();

    @Query("UPDATE Module SET description = :description where Module.id == :id")
    void updateDescription(long id, String description);

    @TypeConverters(SortBy.class)
    @Query("UPDATE Module SET sortBy = :sortBy where Module.id == :id")
    void updateSortBy(long id, SortBy sortBy);

    @Transaction
    @Query("SELECT * FROM Module WHERE Module.id == :id")
    ModuleAndLinks getModuleFromId(long id);

    @Query("DELETE FROM Module WHERE Module.id == :id")
    void deleteModuleFromId(long id);

    @Delete
    void deleteModule(Module module);
}
