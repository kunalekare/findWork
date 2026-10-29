package com.example.skillbridge.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_jobs")
data class CachedJobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val workType: String,
    val status: String,
    val budgetPaise: Long,
    val taskPosterName: String,
    val workerName: String?,
    val locationCity: String,
    val locationAddress: String,
    val urgency: String,
    val matchExplanation: String?,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_categories")
data class CachedCategoryEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameMr: String,
    val workType: String,
    val basePricePaise: Long,
    val isHotspot: Boolean
)

@Entity(tableName = "offline_actions")
data class OfflineActionEntity(
    @PrimaryKey val actionId: String,
    val idempotencyKey: String,
    val actionType: String, // APPLY, ACCEPT_JOB, MARK_COMPLETE, RATE_JOB
    val targetId: String,
    val payloadJson: String,
    val status: String = "QUEUED", // QUEUED, SYNCED, FAILED
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
