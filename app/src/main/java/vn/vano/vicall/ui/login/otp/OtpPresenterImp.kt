package vn.vano.vicall.ui.login.otp

import android.content.Context
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.ui.base.BasePresenterImp
import java.util.concurrent.TimeUnit

class OtpPresenterImp(private val ctx: Context) : BasePresenterImp<OtpView>(ctx) {

    private val userInteractor by lazy { UserInteractor() }

    fun setOtpTimeout(timeOut: Long) {
        view?.also { v ->
            Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    v.onCountingDown(timeOut - it)
                }
                .takeUntil {
                    it == timeOut
                }
                .subscribe()
                .addToCompositeDisposable(compositeDisposable)
        }
    }

    fun loginOtp(phoneNumber: String, otp: String) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (v.otpIsValid()) {
                    showProgressDialog()

                    userInteractor.loginOtp(phoneNumber, otp)
                        .applyIOWithAndroidMainThread()
                        .subscribe(
                            { user ->
                                when {
                                    user.responseIsSuccess() -> {
                                        v.onOtpVerifiedSuccess(user)
                                    }
                                    user.notFound() -> {
                                        v.onUserNotFound()
                                    }
                                    else -> {
                                        v.onApiResponseError(user)
                                    }
                                }

                                dismissProgressDialog()
                            },
                            {
                                dismissProgressDialog()
                                v.onApiCallError()
                            }
                        ).addToCompositeDisposable(compositeDisposable)
                } else {
                    v.onOtpError()
                }
            } else {
                v.onNetworkError()
            }
        }
    }
}