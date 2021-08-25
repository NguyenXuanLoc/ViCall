package vn.vano.vicall.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_contacts.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseFragment

class ContactsFragment : BaseFragment<ContactView, ContactPresenterImp>(), ContactView {

    companion object {
        private const val RC_PERMISSION_READ_CONTACT = 1

        fun newInstance(): ContactsFragment {
            return ContactsFragment()
        }
    }

    private var isViewed = false

    override fun onResume() {
        super.onResume()

        if (!isViewed) {
            checkContactPermission()
            isViewed = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_CONTACT && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkContactPermission()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun initView(): ContactView {
        return this
    }

    override fun initPresenter(): ContactPresenterImp {
        return ContactPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contacts
    }

    override fun initWidgets() {
        // Listeners
        lblGrant.setOnSafeClickListener {
            checkContactPermission(true)
        }
    }

    override fun onContactsLoadedSuccess(contacts: ArrayList<ContactModel>) {
        ctx?.currentUser?.phoneNumberNonPrefix?.run {
            presenter.syncContacts(this, contacts)
        }
    }

    override fun onContactsSyncedSuccess() {
        ctx?.run {
            toast(R.string.sync_success)

            CommonUtil.markContactsAreSynced(ctx)
        }
    }

    private fun initPagerAdapter() {
        pagerContact.adapter = ContactStateAdapter(parentActivity)
        pagerContact.offscreenPageLimit = pagerContact.adapter?.itemCount ?: 1
        pagerContact.isUserInputEnabled = false

        TabLayoutMediator(tabsContact, pagerContact) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.all_contact)
                1 -> getString(R.string.spam_list)
                2 -> getString(R.string.blocked_list)
                else -> ""
            }
        }.attach()
    }

    private fun checkContactPermission(requestPermission: Boolean = false) {
        if (PermissionUtil.isGranted(
                self,
                arrayOf(Manifest.permission.READ_CONTACTS),
                RC_PERMISSION_READ_CONTACT,
                requestPermission
            )
        ) {
            // Init pager adapter
            tabsContact.visible()
            pagerContact.visible()
            initPagerAdapter()

            // Hide permission layout
            llPermission.gone()

            // Sync all contacts if they're not synced
            ctx?.run {
                if (!CommonUtil.contactsAreSynced(ctx)) {
//                    presenter.getContactsFromLocal()
                    ctx.syncContactsToServer()
                }
            }
        } else {
            // Show permission layout
            llPermission.visible()

            // Hide pager layout
            tabsContact.gone()
            pagerContact.gone()
        }
    }
}
