package vn.vano.vicall.ui.test

import android.content.Context
import android.util.Log
import timber.log.Timber
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.data.interactor.UserInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class TestPresenterImp(ctx: Context) : BasePresenterImp<TestView>(ctx) {
    private val userInteractor by lazy { UserInteractor() }
    fun test(test: String) {
        view?.also {
            if (networkIsAvailable()) {
          /*      userInteractor.test(test).applyIOWithAndroidMainThread().subscribe(
                    {
                        Timber.e("OK")
                    }, {
                        Timber.e("ERROR: ${it.message}")
                    }
                )*/
            }
        }
    }
}