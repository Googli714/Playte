package com.example.playte

import android.content.Context
import com.example.playte.ToastUtil.makeToastSuspend
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateUtil {
    suspend fun updateYtDlp(context: Context) {
        withContext(Dispatchers.IO) {
            YoutubeDL.getInstance().updateYoutubeDL(context).apply {
                if (this == YoutubeDL.UpdateStatus.DONE)
                    YoutubeDL.getInstance().version(context)?.let {
                        makeToastSuspend(context, "YT DL updated to $it")
                        println("Updated")
                    }
            }
        }
    }
}