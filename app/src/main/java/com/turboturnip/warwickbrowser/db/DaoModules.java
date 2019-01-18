package com.turboturnip.warwickbrowser.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

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

    @Delete
    void deleteModule(Module module);
}
