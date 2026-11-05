
package com.example.studybetter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ModuleEntity::class,
        AssignmentEntity::class,
        StudySessionEntity::class,
        AcademicResourceEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class StudyBetterDatabase : RoomDatabase() {

    abstract fun moduleDao(): ModuleDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun academicResourceDao(): AcademicResourceDao

    companion object {

        @Volatile
        private var INSTANCE: StudyBetterDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE modules ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'"
                )
                db.execSQL(
                    "ALTER TABLE modules ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE assignments ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'SYNCED'"
                )
                db.execSQL(
                    "ALTER TABLE assignments ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE study_sessions ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS academic_resources (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        resourceUrl TEXT NOT NULL,
                        createdAt TEXT NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): StudyBetterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyBetterDatabase::class.java,
                    "study_better_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
