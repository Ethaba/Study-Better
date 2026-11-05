
package com.example.studybetter.data

class AcademicResourceRepository(context: android.content.Context) {

    private val resourceDao =
        StudyBetterDatabase.getDatabase(context).academicResourceDao()

    suspend fun getResources(userId: String): List<AcademicResourceEntity> {
        return resourceDao.getResources(userId)
    }

    suspend fun searchResources(
        userId: String,
        query: String
    ): List<AcademicResourceEntity> {
        return resourceDao.searchResources(userId, query)
    }

    suspend fun saveResource(resource: AcademicResourceEntity) {
        resourceDao.insertResource(resource)
    }

    suspend fun deleteResource(resourceId: Int, userId: String) {
        resourceDao.deleteResource(resourceId, userId)
    }
}
