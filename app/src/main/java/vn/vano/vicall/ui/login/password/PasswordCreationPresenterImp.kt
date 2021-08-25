package vn.vano.vicall.ui.login.password

import android.content.Context
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class PasswordCreationPresenterImp(private val ctx: Context) :
    BasePresenterImp<PasswordCreationView>(ctx) {

    private val userInteractor by lazy { UserInteractor() }

    fun createPassword(type: String, msisdn: String, password: String) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (v.passwordIsValid()) {
                    if (v.passwordRetypeIsValid()) {
                        showProgressDialog()

                        userInteractor.updateUserSettings(type, msisdn, password)
                            .applyIOWithAndroidMainThread()
                            .subscribe(
                                { user ->
                                    if (user.responseIsSuccess()) {
                                        // Save logged in user into shared preference
                                        CommonUtil.saveLoggedInUserToList(ctx, user)

                                        // Notify to view
                                        v.onPasswordCreatedSuccess(user)

                                        dismissProgressDialog()
                                    } else {
                                        v.onApiResponseError(user)
                                    }
                                },
                                {
                                    dismissProgressDialog()
                                    v.onApiCallError()
                                }
                            ).addToCompositeDisposable(compositeDisposable)
                    } else {
                        v.onPasswordRetypeError()
                    }
                } else {
                    v.onPasswordError()
                }
            } else {
                v.onNetworkError()
            }
        }
    }
}