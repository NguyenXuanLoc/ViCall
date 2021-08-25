package vn.vano.vicall.ui.introduction

import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_intro_item.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.ui.base.BaseFragment

private const val IMAGE_RES = "imageRes"

class IntroItemFragment : BaseFragment<IntroItemView, IntroItemPresenterImp>(), IntroItemView {

    private var imageRes: Int? = null

    companion object {
        @JvmStatic
        fun newInstance(imageRes: Int) = IntroItemFragment().apply {
            arguments = Bundle().apply {
                putInt(IMAGE_RES, imageRes)
            }
        }
    }

    override fun getExtrasValue() {
        arguments?.let {
            imageRes = it.getInt(IMAGE_RES)
        }
    }

    override fun initView(): IntroItemView = this

    override fun initPresenter(): IntroItemPresenterImp = IntroItemPresenterImp(ctx!!)

    override fun getLayoutId(): Int {
        return R.layout.fragment_intro_item
    }

    override fun initWidgets() {
        imageRes?.let {
            imgIntro.setImageResource(it)
        }
    }
}
