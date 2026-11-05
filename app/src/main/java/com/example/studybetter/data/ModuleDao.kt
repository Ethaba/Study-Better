
package com.example.studybetter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    // Used during server refresh. Never overwrite an existing local record.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertServerModules(modules: List<ModuleEntity>)

    @Query("""
        SELECT * FROM modules
        WHERE userId = :userId AND isDeleted = 0
        ORDER BY title ASC
    """)
    suspend fun getModules(userId: String): List<ModuleEntity>

    @Query("""
        SELECT * FROM modules
        WHERE userId = :userId
        AND syncStatus = 'PENDING'
    """)
    suspend fun getPendingModules(userId: String): List<ModuleEntity>

    @Query("""
        UPDATE modules SET syncStatus = 'SYNCED'
        WHERE userId = :userId AND id = :moduleId
    """)
    suspend fun markModuleSynced(userId: String, moduleId: Long)

    @Query("""
        UPDATE modules SET isDeleted = 1, syncStatus = 'PENDING'
        WHERE userId = :userId AND id = :moduleId
    """)
    suspend fun markModuleDeleted(userId: String, moduleId: Long)

    @Query("""
        DELETE FROM modules
        WHERE userId = :userId AND id = :moduleId
        AND syncStatus = 'SYNCED'
    """)
    suspend fun deleteSyncedModule(userId: String, moduleId: Long)

    @Query("""
        DELETE FROM modules
        WHERE userId = :userId AND syncStatus = 'SYNCED'
    """)
    suspend fun deleteAllSyncedModules(userId: String)

    @Query("""
        DELETE FROM modules
        WHERE userId = :userId AND id = :moduleId
    """)
    suspend fun permanentlyDeleteModule(userId: String, moduleId: Long)
}
