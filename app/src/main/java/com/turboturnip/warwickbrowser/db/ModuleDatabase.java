package com.turboturnip.warwickbrowser.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
