package com.example.webapplication

import android.app.AlertDialog
import android.content.Context

object AlertDialoggg {
    fun create(
        context: Context,
        error: Int,
        message: Int,
        function: () -> Unit,
        functionExit: () -> Unit,
        onDismiss: (() -> Unit)? = null
    ): AlertDialog {
        return AlertDialog.Builder(context)
            .apply {
                setTitle(error)
                setMessage(message)
                setPositiveButton(R.string.retry) { _, _ -> function() }
                setNegativeButton(R.string.exit) { _, _ -> functionExit() }
                setCancelable(false)
            }
            .create()
            .also { dialog ->
                onDismiss?.let { callback ->
                    dialog.setOnDismissListener { callback.invoke() }
                }
                dialog.show()
            }
    }
}