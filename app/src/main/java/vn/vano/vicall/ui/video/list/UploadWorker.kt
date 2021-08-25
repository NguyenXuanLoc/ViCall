package vn.vano.vicall.ui.video.list

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.vano.vicall.R
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.createVideoFileFromUri
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.interactor.VideoInteractor
import vn.vano.vicall.data.model.VideoModel
import java.io.File

class UploadWorker(val ctx: Context, workerParams: WorkerParameters) :
    Worker(ctx, workerParams) {

    companion object {
        private var REQUEST_CODE = 1000
        private var NOTIFICATION_ID = 1000
    }

    private val videoInteractor by lazy { VideoInteractor() }
    private var disposable: Disposable? = null

    override fun doWork(): Result {
        // Get the input
        val fileUriPath = inputData.getString(Key.FILE_URI_PATH)
        val phoneNumber = inputData.getString(Key.PHONE_NUMBER)

        fileUriPath?.also { path ->
            val videoFile = ctx.createVideoFileFromUri(Uri.parse(path))
            videoFile?.run {
                phoneNumber?.also { number ->
                    // Show notification
                    showUploadingNotification(R.string.uploading_video)

                    // Call upload api
                    videoInteractor.uploadVideo(number, videoFile)
                        .subscribeOn(Schedulers.io())
                        .subscribe(object : SingleObserver<VideoModel> {
                            override fun onSuccess(response: VideoModel) {
                                // Must delete temp file
                                deleteTempFile(videoFile)

                                // Show notification
                                if (response.responseIsSuccess()) {
                                    showUploadingNotification(R.string.uploaded_video_success)
                                } else {
                                    showUploadingNotification(response.message)
                                }

                                // Dispose
                                if (disposable?.isDisposed == false) {
                                    disposable?.dispose()
                                }
                            }

                            override fun onSubscribe(d: Disposable) {
                                disposable = d
                            }

                            override fun onError(e: Throwable) {
                                // Must delete temp file
                                deleteTempFile(videoFile)

                                // Show notification
                                showUploadingNotification(R.string.failed_to_upload)

                                e.printStackTrace()
                            }
                        })
                }
            }
        }

        return Result.success()
    }

    private fun deleteTempFile(file: File) {
        file.delete()
    }

    private fun showUploadingNotification(message: Any) {
        val pendingIntent =
            PendingIntent.getActivity(
                ctx,
                REQUEST_CODE,
                Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        val channelId = ctx.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val msg = when (message) {
            is CharSequence -> message
            is String -> message
            is Int -> ctx.getString(message)
            else -> ""
        }
        val notificationBuilder = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(ctx.getString(R.string.app_name))
            .setContentText(msg)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        val notificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (PermissionUtil.isApi26orHigher()) {
            val channel = NotificationChannel(
                channelId,
                ctx.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}