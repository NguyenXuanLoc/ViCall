package vn.vano.vicall.ui.video.vistore

import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_vistore.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.ui.base.BaseFragment

class ViStoreFragment : BaseFragment<ViStoreView, ViStorePresenterImp>(), ViStoreView {

    companion object {
        fun newInstance(): ViStoreFragment {
            return ViStoreFragment()
        }
    }

    private var isViewed = false

    override fun onResume() {
        super.onResume()

        if (!isViewed) {
            initPager()
            isViewed = true
        }
    }

    override fun initView(): ViStoreView {
        return this
    }

    override fun initPresenter(): ViStorePresenterImp {
        return ViStorePresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_vistore
    }

    override fun initWidgets() {

    }

    private fun initPager() {
        pagerVideo.adapter = ViStoreStateAdapter(parentActivity)
        pagerVideo.offscreenPageLimit = pagerVideo.adapter?.itemCount ?: 1
        pagerVideo.isUserInputEnabled = false

        TabLayoutMediator(tabsVideo, pagerVideo) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.newest)
                1 -> getString(R.string.used_most)
                2 -> getString(R.string.free_vi)
                else -> ""
            }
        }.attach()
    }
}
