package com.thekami.kamitv.update

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thekami.kamitv.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: UpdateInfo) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    object ReadyToInstall : UpdateState()
    object UpToDate : UpdateState()
}

class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var downloadId: Long = -1
    private val apkFile get() = File(
        getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
        "kamiTV-update.apk"
    )

    fun checkForUpdate() {
        viewModelScope.launch {
            _state.value = UpdateState.Checking
            val info = UpdateChecker.check(BuildConfig.VERSION_NAME)
            _state.value = if (info != null) UpdateState.Available(info) else UpdateState.UpToDate
            if (_state.value == UpdateState.UpToDate) {
                delay(3000)
                _state.value = UpdateState.Idle
            }
        }
    }

    fun downloadAndInstall(info: UpdateInfo) {
        val ctx = getApplication<Application>()
        _state.value = UpdateState.Downloading(0)
        apkFile.delete()

        val req = DownloadManager.Request(Uri.parse(info.downloadUrl))
            .setTitle("kamiTV update")
            .setDescription("v${info.latestVersion}")
            .setDestinationUri(Uri.fromFile(apkFile))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(req)

        ctx.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                if (i.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                    ctx.unregisterReceiver(this)
                    _state.value = UpdateState.ReadyToInstall
                    installApk(ctx)
                }
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            if (Build.VERSION.SDK_INT >= 33) Context.RECEIVER_NOT_EXPORTED else 0)
    }

    private fun installApk(ctx: Context) {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }

    fun dismiss() { _state.value = UpdateState.Idle }
}
