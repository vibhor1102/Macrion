/*
 * Copyright (C) 2026 Vibhor Goel
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package io.github.vibhor1102.macrion.crash

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.vibhor1102.macrion.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CrashReportPrompt : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.crash_report_prompt_title)
            .setMessage(R.string.crash_report_local_notice)
            .setPositiveButton(R.string.crash_report_send) { _, _ -> sendReport() }
            .setNeutralButton(R.string.crash_report_review) { _, _ ->
                startActivity(Intent(requireContext(), CrashReportsActivity::class.java))
            }
            .setNegativeButton(R.string.crash_report_discard) { _, _ ->
                val store = requireContext().applicationContext.crashReportStore()
                val activity = requireActivity()
                val id = requireArguments().getString("reportId") ?: return@setNegativeButton
                activity.lifecycleScope.launch {
                    val deleted = withContext(Dispatchers.IO) { runCatching { store.delete(id) }.isSuccess }
                    if (!deleted) android.widget.Toast.makeText(activity, R.string.crash_reports_error, android.widget.Toast.LENGTH_LONG).show()
                }
            }.create()

    private fun sendReport() {
        val context = requireContext().applicationContext
        val activity = requireActivity()
        val id = requireArguments().getString("reportId") ?: return
        activity.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val store = context.crashReportStore()
                    val report = store.pending().firstOrNull { it.id == id } ?: return@runCatching null
                    CrashReportUploader().send(report.body).also { outcome ->
                        if (outcome == CrashReportUploader.Result.SENT || outcome == CrashReportUploader.Result.ALREADY_SENT) {
                            store.delete(id)
                        }
                    }
                }
            }.getOrNull()
            val message = when (result) {
                CrashReportUploader.Result.SENT, CrashReportUploader.Result.ALREADY_SENT -> R.string.crash_report_sent
                CrashReportUploader.Result.REJECTED -> R.string.crash_report_rejected
                else -> R.string.crash_report_send_failed
            }
            android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TAG = "local-crash-report"
        fun newInstance(id: String) = CrashReportPrompt().apply {
            arguments = Bundle().apply { putString("reportId", id) }
        }
    }
}
