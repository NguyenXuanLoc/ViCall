package vn.vano.vicall.ui.contacts

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.ui.contacts.allcontact.ContactAllFragment
import vn.vano.vicall.ui.contacts.blocked.ContactBlockedFragment
import vn.vano.vicall.ui.contacts.spam.ContactSpamFragment

class ContactStateAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val pages by lazy {
        ArrayList<Fragment>().apply {
            add(ContactAllFragment.newInstance())
            add(ContactSpamFragment.newInstance())

            // Just show blocked list if it's android 7.0 or higher
            if (PermissionUtil.isApi24orHigher()) {
                add(ContactBlockedFragment.newInstance())
            }
        }
    }

    override fun getItemCount(): Int {
        return pages.size
    }

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}