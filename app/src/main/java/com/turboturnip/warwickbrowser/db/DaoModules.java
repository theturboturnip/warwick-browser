package com.turboturnip.warwickbrowser.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

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
