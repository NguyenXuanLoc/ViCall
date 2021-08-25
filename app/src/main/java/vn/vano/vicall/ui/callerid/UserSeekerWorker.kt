package vn.vano.vicall.ui.callerid

import android.content.Context
import android.os.Environment
import androidx.work.*
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.data.model.UserOnCallEventModel
import java.io.File

class UserSeekerWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val userInteractor by lazy { UserInteractor() }
    private var disposable: Disposable? = null

    override fun doWork(): Result {
        // Get the input
        val callerNumber = inputData.getString(Key.PHONE_NUMBER)
        callerNumber?.run {
            userInteractor.getCallerId(context.currentUser?.phoneNumberNonPrefix, callerNumber)
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<UserModel> {
                    override fun onSuccess(user: UserModel) {
                        // Save caller's info and download video
                        if (user.responseIsSuccess()) {
                            // Save caller's info into shared preference
                            CommonUtil.getCallerByPhone(context, callerNumber)?.also { localUser ->
                                // Keep local video path if it's exist
                                user.urlVideoLocal = localUser.urlVideoLocal
                            }
                            CommonUtil.saveCallerByPhone(context, user)

                            // Publish user on call event
                            RxBus.publishUserOnCallEventModel(UserOnCallEventModel(user))

                            // Download incoming video
                            downloadIncomingVideo(context, user)
                        }

                        // Dispose the disposable
                        if (disposable?.isDisposed == false) {
                            disposable?.dispose()
                        }
                    }

                    override fun onSubscribe(d: Disposable) {
                        disposable = d
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
        }

        return Result.success()
    }

    private fun downloadIncomingVideo(ctx: Context, user: UserModel) {
        with(user) {
            urlVideo?.also { url ->
                phoneNumberNonPrefix?.also { phoneNumber ->
                    val fileName = "$phoneNumber${url.substring(url.lastIndexOf("/") + 1)}"
                    val moviesFolder = ctx.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                    val filePath = "$moviesFolder${File.separator}$fileName"
                    val file = File(filePath)

                    // Download user's incoming video if the video is not exist
                    if (!file.exists()) {
                        // Init work manager
                        val constraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                        val inputData = workDataOf(
                            Key.URL to url,
                            Key.FILE_PATH to filePath,
                            Key.PHONE_NUMBER to phoneNumber
                        )
                        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                            .setConstraints(constraints)
                            .setInputData(inputData)
                            .build()
                        WorkManager.getInstance(ctx).enqueue(workRequest)

                        // Searching and removing previous incoming video
                        if (moviesFolder?.exists() == true) {
                            moviesFolder.listFiles()?.forEach { f ->
                                if (f.name.startsWith(phoneNumber)) {
                                    f.delete()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}