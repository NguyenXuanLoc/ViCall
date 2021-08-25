package vn.vano.vicall.ui.pointhistory

import android.content.Context
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.networkIsConnected
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class PointHistoryPresenterImp(val ctx: Context) : BasePresenterImp<PointHistoryView>(ctx) {
    private val userInteractor by lazy { UserInteractor() }

    fun getPointHistory(phoneNumber: String) {
        view?.also { v ->
            if (ctx.networkIsConnected()) {
                showProgressDialog()
                userInteractor.getPointHistory(phoneNumber)
                    .applyIOWithAndroidMainThread()
                    .subscribe({
                        if (it.responseIsSuccess()) {
                            v.onPointLoaded(it)
                        } else {
                            v.onApiResponseError(it)
                        }

                        dismissProgressDialog()
                    }, {
                        it.printStackTrace()
                        v.onApiCallError()
                        dismissProgressDialog()
                    }).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }
}