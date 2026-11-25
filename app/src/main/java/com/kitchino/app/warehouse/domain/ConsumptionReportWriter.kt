package com.kitchino.app.warehouse.domain

import java.io.File

class ConsumptionReportWriter(
    private val reportDir: File,
    private val targetFileFactory: (String) -> File = { name -> File(reportDir, name) },
    private val openWriter: (File) -> java.io.Writer = { file -> file.bufferedWriter() }
) {

    fun write(rows: List<ReportRow>): ReportWriteResult {
        if (rows.isEmpty()) {
            return ReportWriteResult.Error("Нет данных для отчета")
        }
        if (!reportDir.exists() && !reportDir.mkdirs()) {
            return ReportWriteResult.Error("Каталог отчета недоступен: ${reportDir.absolutePath}")
        }
        val file = targetFileFactory("consumption_report_${System.currentTimeMillis()}.csv")
        return try {
            openWriter(file).use { out ->
                out.appendLine("ingredient;amount;timestamp")
                rows.forEach { r ->
                    out.appendLine("${r.ingredient};${r.amount};${r.timestamp}")
                }
            }
            ReportWriteResult.Ok(file.absolutePath)
        } catch (e: Exception) {
            file.delete()
            ReportWriteResult.Error("Ошибка записи отчета: ${e.message}")
        }
    }
}

data class ReportRow(
    val ingredient: String,
    val amount: Double,
    val timestamp: Long
)

sealed interface ReportWriteResult {
    data class Ok(val path: String) : ReportWriteResult
    data class Error(val message: String) : ReportWriteResult
}