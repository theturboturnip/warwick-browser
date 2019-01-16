package com.turboturnip.warwickbrowser.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Database(entities = {Module.class, ModuleLink.class}, version = 1, exportSchema = false)
public abstract class ModuleDatabase extends RoomDatabase {
    public abstract DaoModules daoModules();

    private static volatile ModuleDatabase INSTANCE;
    @Nullable
    public static ModuleDatabase getDatabase() {
        return INSTANCE;
    }
    @NonNull
    public static ModuleDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ModuleDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ModuleDatabase.class,
                            "moduleDB"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
