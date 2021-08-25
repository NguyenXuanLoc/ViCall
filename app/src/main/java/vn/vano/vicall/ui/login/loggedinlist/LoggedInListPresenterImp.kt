package vn.vano.vicall.ui.login.loggedinlist

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import timber.log.Timber
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.ui.base.BasePresenterImp

class LoggedInListPresenterImp(val ctx: Context) : BasePresenterImp<LoggedInListView>(ctx) {

    fun getLoggedInUsers() {
        view?.also { v ->
            showProgressDialog()

            Observable.just(CommonUtil.getLoggedInList(ctx))
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onUsersLoadedSuccess(it)
                        dismissProgressDialog()
                    },
                    {
                        dismissProgressDialog()
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}