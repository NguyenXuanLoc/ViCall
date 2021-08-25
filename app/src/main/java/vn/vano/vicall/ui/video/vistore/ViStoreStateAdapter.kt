package vn.vano.vicall.ui.video.vistore

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.vano.vicall.common.Constant
import vn.vano.vicall.ui.video.list.VideoListFragment

class ViStoreStateAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val pages by lazy {
        arrayListOf<Fragment>(
            VideoListFragment.newInstance(Constant.STORE, Constant.LASTEST),
            VideoListFragment.newInstance(Constant.STORE, Constant.MOST_USED),
            VideoListFragment.newInstance(Constant.STORE, Constant.FREE)
        )
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}