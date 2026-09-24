package com.kitchino.app.core.contracts

interface AuditApi {

    suspend fun logAction(actorId: Long, action: String, payload:String)
}