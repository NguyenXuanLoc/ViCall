package vn.vano.vicall.ui.introduction

import vn.vano.vicall.ui.base.BaseView

interface IntroSliderView : BaseView {

    fun slideIsReadyToShow(imageRes: List<Int>)

    fun onFinishedIntro()

}