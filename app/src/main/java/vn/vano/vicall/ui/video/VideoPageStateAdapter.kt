package vn.vano.vicall.ui.video

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.vano.vicall.ui.video.myvideo.MyVideoFragment
import vn.vano.vicall.ui.video.vistore.ViStoreFragment

class VideoPageStateAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val pages by lazy {
        arrayListOf<Fragment>(
            ViStoreFragment.newInstance(),
            MyVideoFragment.newInstance()
        )
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}