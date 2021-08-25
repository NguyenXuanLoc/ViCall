package vn.vano.vicall.ui.main

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.Observable
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.data.model.UserModel
import java.util.concurrent.TimeUnit

class UserProfileGettingWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val userInteractor by lazy { UserInteractor() }
    private val compositeDisposable by lazy { CompositeDisposable() }

    override fun doWork(): Result {
        // Get the input
        val delayTime = inputData.getLong(Key.COUNT_NUMBER, 0)
        val delay = if (delayTime < 0) {
            0L
        } else {
            delayTime
        }
        val phoneNumber = context.currentUser?.phoneNumberNonPrefix
        phoneNumber?.run {
            Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    if (delay - it == 0L) {
                        userInteractor.getUserProfile(phoneNumber)
                            .subscribeOn(Schedulers.io())
                            .subscribe(object : SingleObserver<UserModel> {
                                override fun onSuccess(user: UserModel) {
                                    if (user.responseIsSuccess()) {
                                        // Save and publish user profile event
                                        CommonUtil.saveLoggedInUserToList(context, user)
                                    }

                                    // Dispose the disposable
                                    compositeDisposable.clear()
                                }

                                override fun onSubscribe(d: Disposable) {
                                    d.addToCompositeDisposable(compositeDisposable)
                                }

                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                }
                            })
                    }
                }
                .takeUntil {
                    it == delay
                }
                .subscribe()
                .addToCompositeDisposable(compositeDisposable)
        }

        return Result.success()
    }
}