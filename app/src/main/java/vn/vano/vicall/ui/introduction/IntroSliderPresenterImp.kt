package vn.vano.vicall.ui.introduction

import android.content.Context
import vn.vano.vicall.R
import vn.vano.vicall.ui.base.BasePresenterImp

class IntroSliderPresenterImp(val context: Context) : BasePresenterImp<IntroSliderView>(context) {

    fun getImagesToShow() {
        val imagesRes: ArrayList<Int> = arrayListOf()
        imagesRes.add(R.drawable.img_intro_1)
        imagesRes.add(R.drawable.img_intro_2)
        imagesRes.add(R.drawable.img_intro_3)
        view?.slideIsReadyToShow(imagesRes)

    }

    fun finishIntroduction() {
        view?.onFinishedIntro()
    }

}