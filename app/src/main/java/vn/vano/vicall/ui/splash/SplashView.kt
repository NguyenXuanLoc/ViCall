package vn.vano.vicall.ui.splash

import vn.vano.vicall.ui.base.BaseView

interface SplashView : BaseView {

    fun openLoginPage()

    fun openHomePage()

    fun openIntroductionPage()

    fun finishSplash()
}