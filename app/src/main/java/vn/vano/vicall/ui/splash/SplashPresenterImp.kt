package vn.vano.vicall.ui.splash

import android.content.Context
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.ui.base.BasePresenterImp
import java.util.concurrent.TimeUnit

private const val DELAY_TIME = 2L

class SplashPresenterImp(private val ctx: Context) : BasePresenterImp<SplashView>(ctx) {

    fun delay() {
        view?.also { v ->
            Completable.timer(DELAY_TIME, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe {
                    CommonUtil.getCurrentUser(ctx)?.run {
                        if (isLoggedIn) {
                            v.openHomePage()
                        } else {
                            v.openLoginPage()
                        }
                    } ?: run {
                        if (CommonUtil.introIsShown(ctx)) {
                            v.openLoginPage()
                        } else {
                            v.openIntroductionPage()
                        }
                    }

                    v.finishSplash()
                }.addToCompositeDisposable(compositeDisposable)
        }
    }
}