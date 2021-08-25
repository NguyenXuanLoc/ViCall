package vn.vano.vicall.common.ext

import android.app.role.RoleManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.work.*
import com.google.firebase.iid.FirebaseInstanceId
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import org.json.JSONObject
import timber.log.Timber
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.MyApplication
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.FileUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.callerid.UserSeekerWorker
import vn.vano.vicall.ui.contacts.ContactSynchronizeWorker
import vn.vano.vicall.ui.login.LoginActivity
import vn.vano.vicall.ui.main.UserProfileGettingWorker
import vn.vano.vicall.ui.notification.detail.NotificationDetailActivity
import vn.vano.vicall.ui.video.list.UploadWorker
import vn.vano.vicall.ui.webview.WebviewActivity
import java.io.File

fun Context.networkIsConnected(): Boolean {
    try {
        val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return conMgr?.activeNetworkInfo?.isConnected ?: false
    } catch (e: Exception) {
        Timber.e("$e")
    }

    return false
}

fun Context.getFirebaseInstanceToken(onComplete: (String?) -> Unit) {
    FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
        if (it.isSuccessful) {
            it.result?.run {
                onComplete(token)
            } ?: onComplete(null)
        } else {
            onComplete(null)
        }
    }
}

fun Context.showNetworkError() {
    toast(R.string.err_network_not_available)
}

fun Context.showApiCallError(code: Int) {
    toast(String.format(getString(R.string.err_common), code))
}

fun Context.requestDrawOverlayPermission(requestCode: Int) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:$packageName")
    )
    when (this) {
        is AppCompatActivity -> {
            startActivityForResult(intent, requestCode)
        }
        is Fragment -> {
            startActivityForResult(intent, requestCode)
        }
    }
}

fun Context.openAppSettingsPage() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        data = Uri.parse("package:$packageName")
    }.run {
        startActivity(this)
    }
}

// Create worker to get user's info
fun Context.getCallerId(phoneNumber: String) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val inputData = workDataOf(Key.PHONE_NUMBER to phoneNumber)
    val workRequest = OneTimeWorkRequestBuilder<UserSeekerWorker>()
        .setConstraints(constraints)
        .setInputData(inputData)
        .build()
    WorkManager.getInstance(this).enqueue(workRequest)
}

// Create worker to get user's profile
fun Context.getUserProfile(delayTime: Long? = null) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val workRequestBuilder = OneTimeWorkRequestBuilder<UserProfileGettingWorker>()
        .setConstraints(constraints)
    delayTime?.run {
        val inputData = workDataOf(Key.COUNT_NUMBER to delayTime)
        workRequestBuilder.setInputData(inputData)
    }
    WorkManager.getInstance(this).enqueue(workRequestBuilder.build())
}

// Create worker to upload video
fun Context.uploadVideo(phoneNumber: String, fileUri: Uri) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val inputData = workDataOf(
        Key.FILE_URI_PATH to fileUri.toString(),
        Key.PHONE_NUMBER to phoneNumber
    )
    val workRequest = OneTimeWorkRequestBuilder<UploadWorker>()
        .setConstraints(constraints)
        .setInputData(inputData)
        .build()
    WorkManager.getInstance(this).enqueue(workRequest)
}

// Create worker to sync contacts
fun Context.syncContactsToServer() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val workRequest = OneTimeWorkRequestBuilder<ContactSynchronizeWorker>()
        .setConstraints(constraints)
        .build()
    WorkManager.getInstance(this).enqueue(workRequest)
}

fun Context.isDefaultDialerApp(): Boolean {
    val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager?
    return packageName == telecomManager?.defaultDialerPackage
}

fun Context.requestDefaultDialerApp(requestContext: Any, requestCode: Int) {
    if (PermissionUtil.isApi29orHigher()) {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        when (requestContext) {
            is Fragment -> requestContext.startActivityForResult(intent, requestCode)
            is AppCompatActivity -> requestContext.startActivityForResult(intent, requestCode)
        }
    } else {
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
        when (requestContext) {
            is Fragment -> requestContext.startActivityForResult(intent, requestCode)
            is AppCompatActivity -> requestContext.startActivityForResult(intent, requestCode)
        }
    }
}

fun Context.isBlocked(number: String): Boolean {
    return PermissionUtil.isApi24orHigher() && isDefaultDialerApp() && BlockedNumberContract.isBlocked(
        this,
        number
    )
}

fun Context.unblockNumber(number: String): Boolean {
    var deletedRows = 0
    if (PermissionUtil.isApi24orHigher() && isDefaultDialerApp()) {
        val values = ContentValues().apply {
            put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        }
        contentResolver.insert(
            BlockedNumberContract.BlockedNumbers.CONTENT_URI,
            values
        )?.run {
            deletedRows = contentResolver.delete(this, null, null)
        }
    }

    return deletedRows > 0
}

fun Context.addToBlockList(number: String): Boolean {
    var uri: Uri? = null
    if (PermissionUtil.isApi24orHigher() && isDefaultDialerApp()) {
        val values = ContentValues().apply {
            put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        }
        uri = contentResolver.insert(
            BlockedNumberContract.BlockedNumbers.CONTENT_URI,
            values
        )
    }

    return uri != null
}

fun Context.videoSizeIsValid(fileUri: Uri): Boolean {
    val fd = contentResolver.openFileDescriptor(fileUri, "r")
    return fd?.use {
        it.statSize / 1000f / 1000f <= Constant.MAXIMUM_VIDEO_SIZE
    } ?: false
}

fun Context.createImageFileFromUri(uri: Uri?): File? {
    var result: File? = null
    uri?.also {
        contentResolver.openInputStream(uri)?.use { stream ->
            result = FileUtil.createTempImageFile(this)
            result?.also { file ->
                FileUtil.copyStreamToFile(stream, file)
            }
        }
    }

    return result
}

fun Context.createVideoFileFromUri(uri: Uri?): File? {
    var result: File? = null
    uri?.also {
        contentResolver.openInputStream(uri)?.use { stream ->
            result = FileUtil.createTempVideoFile(this)
            result?.also { file ->
                FileUtil.copyStreamToFile(stream, file)
            }
        }
    }

    return result
}

fun Context.convertMapToJSONObject(map: Map<String, Any?>): JSONObject {
    val jsonObject = JSONObject()
    for (key in map.keys) {
        val value = if (map[key] is Map<*, *>) {
            convertMapToJSONObject(map[key] as Map<String, Any?>).toString()
        } else {
            map[key]
        }
        jsonObject.put(key, value)
    }

    return jsonObject
}

fun Context.createNotificationIntent(
    activityModel: ActivityModel,
    addNewTaskFlag: Boolean = true
): Intent {
    Timber.e("activityModel: ${activityModel.toJson()}")
    return if (CommonUtil.getCurrentUser(ctx)?.isLoggedIn == true) {
        with(activityModel) {
            when (actionType) {
                Constant.NOTIFY, Constant.NOTIFY_SELF_APP -> {
                    Intent(ctx, NotificationDetailActivity::class.java).apply {
                        putExtra(Key.ACTIVITY_MODEL, activityModel)
                    }
                }
                Constant.NOTIFY_HTML -> {
                    if (hasActionButton) {
                        buttonActionUrl?.run {
                            Intent(ctx, WebviewActivity::class.java).apply {
                                putExtra(Key.URL, buttonActionUrl)
                                putExtra(Key.HAS_ACTION_BUTTON, true)
                            }
                        } ?: Intent()
                    } else {
                        content?.run {
                            Intent(ctx, WebviewActivity::class.java).apply {
                                putExtra(Key.URL, content)
                            }
                        } ?: Intent()
                    }
                }
                Constant.REG_SERVICE -> {
                    // Save service status to current user
                    currentUser?.apply {
                        isActiveService = true
                    }?.run {
                        CommonUtil.saveLoggedInUserToList(this@createNotificationIntent, this)
                    }

                    Intent()
                }
                Constant.UNREG_SERVICE -> {
                    // Save service status to current user
                    currentUser?.apply {
                        isActiveService = false
                    }?.run {
                        CommonUtil.saveLoggedInUserToList(this@createNotificationIntent, this)
                    }

                    Intent()
                }
                else -> {
                    Intent()
                }
            }
        }
    } else {
        Intent(this, LoginActivity::class.java)
    }.apply {
        if (addNewTaskFlag) {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}

val Context.currentUser: UserModel?
    get() = MyApplication.instance.currentUser

val Context.sharedPref: SharedPreferences
    get() = MyApplication.instance.sharedPref