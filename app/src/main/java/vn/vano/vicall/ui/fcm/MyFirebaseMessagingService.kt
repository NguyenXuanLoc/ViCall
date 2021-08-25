package vn.vano.vicall.ui.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.jetbrains.anko.ctx
import timber.log.Timber
import vn.vano.vicall.R
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.convertMapToJSONObject
import vn.vano.vicall.common.ext.createNotificationIntent
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.mapper.convertToModel
import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.data.response.ActivityResponse

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private var REQUEST_CODE = 0
        private var NOTIFICATION_ID = 0
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Timber.d("From: ${remoteMessage.from}")
        Timber.d("Message notification: ${remoteMessage.notification?.body}")
        Timber.d("Message data: ${remoteMessage.data}")

        // Check if message contains a data payload.
        with(remoteMessage.data) {
            if (isNotEmpty()) {
                /*if (*//* Check if data needs to be processed by long running job *//* true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                    scheduleJob()
                } else {
                    // Handle message within 10 seconds
                    handleNow()
                }*/
                val jsonObject = convertMapToJSONObject(this)
                val activityResponse =
                    Gson().fromJson(jsonObject.toString(), ActivityResponse::class.java)
                val activityModel = activityResponse.convertToModel()

                sendNotification(activityModel)

                // Push user changed event
                currentUser?.run {
                    unreadNotification += 1
                    CommonUtil.saveLoggedInUserToList(ctx, this)
                }
            }
        }
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Timber.d("Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        /*val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        val myJob = dispatcher.newJobBuilder()
                .setService(MyJobService::class.java)
                .setTag("my-job-tag")
                .build()
        dispatcher.schedule(myJob)*/
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Timber.d("Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        // Init work manager
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = workDataOf(Key.FIREBASE_TOKEN to token)
        val workRequest = OneTimeWorkRequestBuilder<FirebaseTokenUpdatingWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(ctx).enqueue(workRequest)
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(activityModel: ActivityModel) {
        val intent = createNotificationIntent(activityModel)
        REQUEST_CODE++
        val pendingIntent =
            PendingIntent.getActivity(this, REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(activityModel.title)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (PermissionUtil.isApi26orHigher()) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        NOTIFICATION_ID++
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}