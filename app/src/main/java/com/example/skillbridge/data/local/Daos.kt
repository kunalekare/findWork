package com.example.skillbridge.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM cached_jobs ORDER BY updatedAt DESC")
    fun getAllJobs(): Flow<List<CachedJobEntity>>

    @Query("SELECT * FROM cached_jobs WHERE id = :jobId LIMIT 1")
    suspend fun getJobById(jobId: String): CachedJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<CachedJobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: CachedJobEntity)

    @Query("UPDATE cached_jobs SET status = :targetStatus, updatedAt = :timestamp WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: String, targetStatus: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM cached_categories")
    fun getAllCategories(): Flow<List<CachedCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CachedCategoryEntity>)
}

@Dao
interface OfflineActionDao {
    @Query("SELECT * FROM offline_actions WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    suspend fun getPendingActions(): List<OfflineActionEntity>

    @Query("SELECT * FROM offline_actions WHERE status = 'QUEUED' ORDER BY createdAt ASC")
    fun observePendingActions(): Flow<List<OfflineActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueAction(action: OfflineActionEntity)

    @Query("UPDATE offline_actions SET status = :status WHERE actionId = :actionId")
    suspend fun updateActionStatus(actionId: String, status: String)

    @Query("DELETE FROM offline_actions WHERE actionId = :actionId")
    suspend fun deleteAction(actionId: String)
}
