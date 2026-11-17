package com.kitchino.app.employee.domain

import com.kitchino.app.employee.data.AuditLogEntity
import java.security.MessageDigest

/**
 * Хеш-цепочка журнала действий (FR-14, FR-16).
 * hash = SHA-256(prevHash | actorId | action | payload | timestamp)
 */
object AuditChain {

    fun computeHash(
        prevHash: String,
        actorId: Long,
        action: String,
        payload: String,
        timestamp: Long
    ): String {
        val raw = "$prevHash|$actorId|$action|$payload|$timestamp"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** Создать запись, сцепленную с предыдущей (FR-14) */
    fun append(
        prev: AuditLogEntity?,
        actorId: Long,
        action: String,
        payload: String,
        timestamp: Long = System.currentTimeMillis()
    ): AuditLogEntity {
        val prevHash = prev?.hash ?: ""
        return AuditLogEntity(
            timestamp = timestamp,
            actorId = actorId,
            action = action,
            payload = payload,
            prevHash = prevHash,
            hash = computeHash(prevHash, actorId, action, payload, timestamp)
        )
    }

    /**
     * Проверить цепочку (FR-16):
     * null — цела; иначе id первой подменённой записи.
     */
    fun verifyChain(logs: List<AuditLogEntity>): Long? {
        var prevHash = ""
        for (log in logs) {
            if (log.prevHash != prevHash) return log.id
            val expected = computeHash(prevHash, log.actorId, log.action, log.payload, log.timestamp)
            if (log.hash != expected) return log.id
            prevHash = log.hash
        }
        return null
    }
}

