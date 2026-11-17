package com.kitchino.app.employee.domain

import com.kitchino.app.core.contracts.AuditApi
import com.kitchino.app.employee.data.AuditLogDao

/**
 * Реализация контракта аудита: действие пишется в хеш-цепочку журнала.
 */
class AuditApiImpl(
    private val auditLogDao: AuditLogDao
) : AuditApi {

    override suspend fun logAction(actorId: Long, action: String, payload: String) {
        val prev = auditLogDao.getLastLog()
        auditLogDao.insertLog(
            AuditChain.append(prev, actorId, action, payload)
        )
    }
}