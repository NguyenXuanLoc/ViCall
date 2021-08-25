package vn.vano.vicall.ui.video

import kotlinx.android.synthetic.main.fragment_video_page.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.ui.base.BaseFragment

class VideoPageFragment : BaseFragment<VideoPageView, VideoPagePresenterImp>(), VideoPageView {

    companion object {

        fun newInstance(): VideoPageFragment {
            return VideoPageFragment()
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

    override fun initView(): VideoPageView {
        return this
    }

    override fun initPresenter(): VideoPagePresenterImp {
        return VideoPagePresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_video_page
    }

    override fun initWidgets() {
    }

    private fun initPager() {
        pagerVideoList.adapter = VideoPageStateAdapter(parentActivity)
        pagerVideoList.offscreenPageLimit = pagerVideoList.adapter?.itemCount ?: 1
        pagerVideoList.isUserInputEnabled = false
    }

    fun switchVideoPages() {
        if (pagerVideoList.currentItem == 0) {
            pagerVideoList.currentItem = 1
        } else if (pagerVideoList.currentItem == 1) {
            pagerVideoList.currentItem = 0
        }
    }
}
