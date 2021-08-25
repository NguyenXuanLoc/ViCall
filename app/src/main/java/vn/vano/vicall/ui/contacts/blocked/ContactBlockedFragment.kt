package vn.vano.vicall.ui.contacts.blocked

import android.content.Intent
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_contact_blocked.*
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity

class ContactBlockedFragment : BaseFragment<ContactBlockedView, ContactBlockedPresenterImp>(),
    ContactBlockedView, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private const val RC_DEFAULT_DIALER = 256

        fun newInstance(): ContactBlockedFragment {
            return ContactBlockedFragment()
        }
    }

    private var isViewed = false
    private val contactsBlocked by lazy { ArrayList<ContactModel>() }
    private val adapter by lazy {
        ContactBlockedAdapter(contactsBlocked,
            showInfoListener = {
                ctx?.run {
                    ContactDetailActivity.start(this, it)
                }
            },
            unblockListener = {
                presenter.unblockContact(it)
            })
    }

    override fun onResume() {
        super.onResume()

        if (!isViewed) {
            getBlockedList()
            isViewed = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_DEFAULT_DIALER && isDefaultDialerApp()) {
            // Get blocked list
            getBlockedList()

            // Hide default dialer permission layout
            llPermission.gone()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun initView(): ContactBlockedView {
        return this
    }

    override fun initPresenter(): ContactBlockedPresenterImp {
        return ContactBlockedPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contact_blocked
    }

    override fun initWidgets() {
        swpContactBlocked.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
        swpContactBlocked.setOnRefreshListener(this)
        rclContactBlocked.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclContactBlocked.setHasFixedSize(true)
        rclContactBlocked.addItemDecoration(
            DividerItemDecoration(
                ctx,
                DividerItemDecoration.VERTICAL
            )
        )
        rclContactBlocked.adapter = adapter

        if (!isDefaultDialerApp()) {
            llPermission.visible()

            lblGrant.setOnSafeClickListener {
                requestDefaultDialerApp()
            }
        }
    }

    override fun dismissRefreshIcon() {
        swpContactBlocked.isRefreshing = false
    }

    override fun onRefresh() {
        getBlockedList()
    }

    override fun onContactsBlockedLoaded(contacts: List<ContactModel>) {
        contactsBlocked.clear()
        contactsBlocked.addAll(contacts)
        adapter.notifyDataSetChanged()

        // Show/hide 'no data' layout
        toggleNoDataLayout()
    }

    override fun onUnblockedSuccess(contact: ContactModel) {
        val removedIndex = contactsBlocked.indexOfFirst {
            it.number == contact.number
        }
        if (removedIndex > -1) {
            contactsBlocked.removeAt(removedIndex)
            adapter.notifyItemRemoved(removedIndex)
            ctx?.toast(String.format(getString(R.string.unblock_success), contact.number))

            // Show/hide 'no data' layout
            toggleNoDataLayout()
        }
    }

    private fun getBlockedList() {
        if (isDefaultDialerApp()) {
            ctx?.currentUser?.phoneNumberNonPrefix?.run {
                presenter.getBlockedList()
            }
        } else {
            dismissRefreshIcon()
        }
    }

    private fun toggleNoDataLayout() {
        if (contactsBlocked.isNotEmpty()) {
            rclContactBlocked.visible()
            lblNoData.gone()
        } else {
            rclContactBlocked.gone()
            lblNoData.visible()
        }
    }

    private fun isDefaultDialerApp(): Boolean {
        return ctx?.isDefaultDialerApp() ?: false
    }

    private fun requestDefaultDialerApp() {
        ctx?.requestDefaultDialerApp(self, RC_DEFAULT_DIALER)
    }
}
