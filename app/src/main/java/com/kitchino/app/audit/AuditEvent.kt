package com.kitchino.app.audit

import org.json.JSONException
import org.json.JSONObject

data class AuditEvent(
    val batchId: Long,
    val action: String,
    val discountPercent: Double?,
    val reason: String
)

fun AuditEvent.toJson(): String {
    return JSONObject()
        .put("batchId", batchId)
        .put("action", action)
        .put("discountPercent", discountPercent ?: JSONObject.NULL)
        .put("reason", reason)
        .toString()
}

@Throws(JSONException::class)
fun auditEventFromJson(frame: String): AuditEvent {
    val json = JSONObject(frame)
    return AuditEvent(
        batchId = json.getLong("batchId"),
        action = json.getString("action"),
        discountPercent = if (json.isNull("discountPercent")) null else json.getDouble("discountPercent"),
        reason = json.getString("reason")
    )
}
