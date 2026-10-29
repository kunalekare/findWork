package com.example.skillbridge.data.offline

import com.example.skillbridge.data.local.OfflineActionDao
import com.example.skillbridge.data.local.OfflineActionEntity
import com.example.skillbridge.data.remote.AdvanceStatusDto
import com.example.skillbridge.data.remote.ReleaseEscrowDto
import com.example.skillbridge.data.remote.SkillBridgeApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Offline Action Queue with Idempotency Protection
 * Queues critical actions while offline or in Low-Data Mode:
 * 1. APPLY
 * 2. ACCEPT_JOB
 * 3. MARK_COMPLETE
 * 4. RATE_JOB
 */
class OfflineActionQueue(
    private val offlineActionDao: OfflineActionDao? = null,
    private val apiService: SkillBridgeApiService? = null
) {

    private val inMemoryQueue = mutableListOf<OfflineActionEntity>()
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    enum class ActionType {
        APPLY,
        ACCEPT_JOB,
        MARK_COMPLETE,
        RATE_JOB
    }

    suspend fun enqueue(
        actionType: ActionType,
        targetId: String,
        payloadJson: String = "{}"
    ): String {
        val idempotencyKey = UUID.randomUUID().toString()
        val action = OfflineActionEntity(
            actionId = UUID.randomUUID().toString(),
            idempotencyKey = idempotencyKey,
            actionType = actionType.name,
            targetId = targetId,
            payloadJson = payloadJson,
            status = "QUEUED",
            createdAt = System.currentTimeMillis()
        )

        offlineActionDao?.enqueueAction(action)
        synchronized(inMemoryQueue) {
            inMemoryQueue.add(action)
            _pendingCount.value = inMemoryQueue.size
        }
        return idempotencyKey
    }

    suspend fun syncAll(): Int {
        var syncedCount = 0
        val pendingList = offlineActionDao?.getPendingActions() ?: synchronized(inMemoryQueue) { inMemoryQueue.toList() }

        for (action in pendingList) {
            try {
                if (apiService != null) {
                    when (action.actionType) {
                        ActionType.ACCEPT_JOB.name -> {
                            apiService.acceptJob(action.targetId, "w_current")
                        }
                        ActionType.MARK_COMPLETE.name -> {
                            apiService.advanceStatus(action.targetId, AdvanceStatusDto("COMPLETED"))
                        }
                        ActionType.RATE_JOB.name -> {
                            apiService.releaseEscrow(action.targetId, action.idempotencyKey, ReleaseEscrowDto(5.0f))
                        }
                        else -> { /* No-op for local simulated */ }
                    }
                }
                offlineActionDao?.deleteAction(action.actionId)
                synchronized(inMemoryQueue) {
                    inMemoryQueue.removeAll { it.actionId == action.actionId }
                    _pendingCount.value = inMemoryQueue.size
                }
                syncedCount++
            } catch (e: Exception) {
                offlineActionDao?.updateActionStatus(action.actionId, "FAILED")
            }
        }
        return syncedCount
    }

    fun getPendingActionsInMemory(): List<OfflineActionEntity> {
        return synchronized(inMemoryQueue) { inMemoryQueue.toList() }
    }
}
