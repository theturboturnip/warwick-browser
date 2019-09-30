package com.turboturnip.warwickbrowser.db;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.turboturnip.warwickbrowser.SortBy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Module.class, ModuleLink.class}, version = 2, exportSchema = false)
public abstract class ModuleDatabase extends RoomDatabase {
    public abstract DaoModules daoModules();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Module ADD sortBy INTEGER DEFAULT("
                    + SortBy.Default.SQLIndex + ")");
            database.execSQL("ALTER TABLE Module ADD description TEXT");
        }
    };

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
                    ).addMigrations(MIGRATION_1_2).build();
                }
            }
        }
        return INSTANCE;
    }
}
