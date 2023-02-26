package com.example.playte

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object ToastUtil {
    var applicationScope = CoroutineScope(SupervisorJob())

    fun makeToast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun makeToastSuspend(context: Context, text: String) {
        applicationScope.launch(Dispatchers.Main) {
            makeToast(context, text)
        }
    }
}