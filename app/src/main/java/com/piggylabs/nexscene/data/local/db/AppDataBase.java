package com.piggylabs.nexscene.data.local.db;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.piggylabs.nexscene.data.local.dao.TitleStateDao;
import com.piggylabs.nexscene.data.local.entity.TitleStateEntity;

@Database(
        entities = {TitleStateEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDataBase extends RoomDatabase {

    public abstract TitleStateDao titleStateDao();

    private static volatile AppDataBase Instance;

    public static AppDataBase getDatabase(Context context) {
        if (Instance != null) {
            return Instance;
        }
        synchronized (AppDataBase.class) {
            if (Instance == null) {
                Instance = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AppDataBase.class,
                                "app_database"
                        )
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return Instance;
        }
    }

    @Nullable
    public static AppDataBase getExistingInstance() {
        return Instance;
    }

    public static void clearInstance() {
        Instance = null;
    }
}
