package vn.vano.vicall.ui.callerid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.ui.main.MainActivity

class InCallServiceImp : InCallService() {

    companion object {
        private const val REQUEST_CODE = 1
        private const val NOTIFICATION_ID = 1
    }

    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)
        val intent = Intent(ctx, CallActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showIncomingNotification(call: Call?) {
        // Create an intent which triggers your fullscreen incoming call user interface.
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClass(ctx, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(ctx, REQUEST_CODE, intent, 0)

        // Build the notification as an ongoing high priority item; this ensures it will show as
        // a heads up notification which slides down over top of the current content.
        val channelId = "incomingCallsChannel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(call?.details?.handle?.schemeSpecificPart ?: "Callerrrrr name")
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, getString(R.string.app_name), IMPORTANCE_HIGH)
            channel.setSound(
                defaultSoundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}