package vn.vano.vicall.ui.fcm

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.data.model.TokenModel

class FirebaseTokenUpdatingWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val userInteractor by lazy { UserInteractor() }
    private var disposable: Disposable? = null

    override fun doWork(): Result {
        // Get the input
        val firebaseToken = inputData.getString(Key.FIREBASE_TOKEN)
        firebaseToken?.run {
            userInteractor.getDeviceToken(firebaseToken)
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<TokenModel> {
                    override fun onSuccess(tokenModel: TokenModel) {
                        // Save device token into shared pref
                        if (tokenModel.responseIsSuccess()) {
                            tokenModel.token?.run {
                                CommonUtil.saveDeviceToken(context, this)
                            }
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
}