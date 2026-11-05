
package com.example.studybetter.data

import android.content.Context
import androidx.room.withTransaction
import com.example.studybetter.Module
import com.example.studybetter.ModuleStorage
import com.example.studybetter.sync.SyncScheduler

class ModuleRoomRepository(context: Context) {

    private val appContext = context.applicationContext
    private val database = StudyBetterDatabase.getDatabase(appContext)
    private val dao = database.moduleDao()

    private fun userId(): String {
        val id = appContext.getSharedPreferences(
            "study_better_settings",
            Context.MODE_PRIVATE
        ).getInt("user_id", -1)

        return id.toString()
    }

    suspend fun migrateOldModules() {
        val preferences = appContext.getSharedPreferences(
            "study_better_room_migration",
            Context.MODE_PRIVATE
        )

        val migrationKey = "modules_migrated_${userId()}"

        if (preferences.getBoolean(migrationKey, false)) return

        val oldModules = ModuleStorage.getModules(appContext)

        oldModules.forEach { module ->
            dao.insertModule(
                ModuleEntity(
                    userId = userId(),
                    id = module.id,
                    code = module.code,
                    title = module.title,
                    description = module.description,
                    syncStatus = if (module.id > Int.MAX_VALUE) {
                        "PENDING"
                    } else {
                        "SYNCED"
                    }
                )
            )
        }

        preferences.edit()
            .putBoolean(migrationKey, true)
            .apply()

        if (oldModules.any { it.id > Int.MAX_VALUE }) {
            SyncScheduler.requestImmediateSync(appContext)
        }
    }

    suspend fun getModules(): List<Module> {
        migrateOldModules()
        return dao.getModules(userId()).map { it.toModule() }
    }

    suspend fun getPendingModules(): List<ModuleEntity> {
        migrateOldModules()
        return dao.getPendingModules(userId())
    }

    suspend fun saveModule(module: Module, pending: Boolean) {
        migrateOldModules()

        dao.insertModule(
            ModuleEntity(
                userId = userId(),
                id = module.id,
                code = module.code,
                title = module.title,
                description = module.description,
                syncStatus = if (pending) "PENDING" else "SYNCED",
                isDeleted = false
            )
        )

        if (pending) {
            SyncScheduler.requestImmediateSync(appContext)
        }
    }

    suspend fun deleteModule(moduleId: Long) {
        migrateOldModules()

        val module = dao.getModules(userId())
            .firstOrNull { it.id == moduleId }

        if (module != null) {
            if (module.id > Int.MAX_VALUE) {
                dao.permanentlyDeleteModule(userId(), moduleId)
            } else {
                dao.markModuleDeleted(userId(), moduleId)
                SyncScheduler.requestImmediateSync(appContext)
            }
        }
    }

    suspend fun permanentlyDeleteModule(moduleId: Long) {
        dao.permanentlyDeleteModule(userId(), moduleId)
    }

    suspend fun markSynced(moduleId: Long) {
        dao.markModuleSynced(userId(), moduleId)
    }

    suspend fun replaceLocalModuleId(
        oldId: Long,
        serverModule: Module
    ) {
        val currentUserId = userId()

        database.withTransaction {
            dao.permanentlyDeleteModule(currentUserId, oldId)

            dao.insertModule(
                ModuleEntity(
                    userId = currentUserId,
                    id = serverModule.id,
                    code = serverModule.code,
                    title = serverModule.title,
                    description = serverModule.description,
                    syncStatus = "SYNCED",
                    isDeleted = false
                )
            )
        }
    }


    suspend fun replaceServerModules(serverModules: List<Module>) {
        migrateOldModules()

        database.withTransaction {
            // Remove old server records, but retain pending offline changes.
            dao.deleteAllSyncedModules(userId())

            val entities = serverModules.map { module ->
                ModuleEntity(
                    userId = userId(),
                    id = module.id,
                    code = module.code,
                    title = module.title,
                    description = module.description,
                    syncStatus = "SYNCED",
                    isDeleted = false
                )
            }

            // IGNORE prevents server records from overwriting pending local records.
            if (entities.isNotEmpty()) {
                dao.insertServerModules(entities)
            }
        }
    }


    private fun ModuleEntity.toModule(): Module {
        return Module(
            id = id,
            code = code,
            title = title,
            description = description
        )
    }
}
