package vn.vano.vicall.ui.registerservice

import android.content.Context
import timber.log.Timber
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.networkIsConnected
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class RegisterServicePresenterImp(val ctx: Context) : BasePresenterImp<RegisterServiceView>(ctx) {
    private val userInteractor by lazy { UserInteractor() }

    fun getConfig() {
        view?.also { v ->
            if (ctx.networkIsConnected()) {
                showProgressDialog()
                userInteractor.config()
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        { it ->
                            v.configLoaded(it)
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
}