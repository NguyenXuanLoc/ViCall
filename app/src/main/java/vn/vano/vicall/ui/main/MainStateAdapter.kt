package vn.vano.vicall.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.vano.vicall.ui.contacts.ContactsFragment
import vn.vano.vicall.ui.home.HomeFragment
import vn.vano.vicall.ui.settings.SettingsFragment
import vn.vano.vicall.ui.video.VideoPageFragment

class MainStateAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val pages by lazy {
        arrayListOf(
            HomeFragment.newInstance(), ContactsFragment.newInstance(), Fragment(),
            VideoPageFragment.newInstance(), SettingsFragment.newInstance()
        )
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}