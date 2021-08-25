package vn.vano.vicall.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.vano.vicall.ui.home.blockedcall.CallBlockedFragment
import vn.vano.vicall.ui.home.callhistory.CallHistoryFragment
import vn.vano.vicall.ui.home.spamcall.CallSpamFragment

class HomeStateAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val pages by lazy {
        arrayListOf<Fragment>(
            CallHistoryFragment.newInstance(),
            CallSpamFragment.newInstance(),
            CallBlockedFragment.newInstance()
        )
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}