package vn.vano.vicall.ui.login

import android.content.Context
import android.widget.EditText
import vn.vano.vicall.common.ErrorCode
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.getFirebaseInstanceToken
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.RxSearchObservable
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class LoginPresenterImp(val ctx: Context) : BasePresenterImp<LoginView>(ctx) {

    private val userInteractor by lazy { UserInteractor() }

    fun getUserByPhone(phoneNumber: String) {
        view?.also { v ->
            if (v.phoneNumberIsValid()) {
                v.onGettingUserByPhoneSuccess(CommonUtil.getLoggedInUserByPhone(ctx, phoneNumber))
            } else {
                v.onPhoneNumberError()
            }
        }
    }

    fun getOtp(phoneNumber: String) {
        view?.also { v ->
            if (networkIsAvailable()) {
                showProgressDialog()

                userInteractor.getOtp(phoneNumber)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            if (it.responseIsSuccess()) {
                                v.onGetOtpSuccess(it, phoneNumber)
                            } else {
                                v.onApiResponseError(it)
                            }
                            dismissProgressDialog()
                        },
                        {
                            dismissProgressDialog()
                            v.onApiCallError()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun getDeviceToken() {
        view?.also { v ->
            if (networkIsAvailable()) {
                showProgressDialog()

                ctx.getFirebaseInstanceToken { token ->
                    token?.run {
                        userInteractor.getDeviceToken(token)
                            .applyIOWithAndroidMainThread()
                            .subscribe(
                                {
                                    if (it.responseIsSuccess()) {
                                        it.token?.run {
                                            CommonUtil.saveDeviceToken(ctx, this)
                                            v.onGetDeviceTokenSuccess(this)
                                        } ?: run {
                                            v.onApiCallError(code = ErrorCode.DEVICE_TOKEN_NULL)
                                        }
                                    } else {
                                        v.onApiResponseError(it)
                                    }
                                    dismissProgressDialog()
                                },
                                {
                                    dismissProgressDialog()
                                    v.onApiCallError()
                                }
                            ).addToCompositeDisposable(compositeDisposable)
                    } ?: run {
                        dismissProgressDialog()
                        v.onApiCallError(code = ErrorCode.FIREBASE_INSTANCE_TOKEN_NULL)
                    }
                }
            } else {
                v.onNetworkError()
            }
        }
    }

    fun registerPhoneTypingListener(txtPhone: EditText) {
        view?.let { v ->
            RxSearchObservable.fromView(txtPhone)
                .applyIOWithAndroidMainThread()
                .subscribe {
                    v.onTypingPhoneNumber(it)
                }.addToCompositeDisposable(compositeDisposable)
        }
    }

    fun loginPassword(phoneNumber: String, password: String) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (v.passwordIsValid()) {
                    showProgressDialog()

                    userInteractor.loginPassword(phoneNumber, password)
                        .applyIOWithAndroidMainThread()
                        .subscribe(
                            { user ->
                                when {
                                    user.responseIsSuccess() -> {
                                        // Save logged in user into shared preference
                                        CommonUtil.saveLoggedInUserToList(ctx, user)

                                        // Notify to view
                                        v.onLoginSuccess(user)
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
                    v.onPasswordError()
                }
            } else {
                v.onNetworkError()
            }
        }
    }
}