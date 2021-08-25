package vn.vano.vicall.ui.contacts.spam

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_contact_spam.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity

class ContactSpamFragment : BaseFragment<ContactSpamView, ContactSpamPresenterImp>(),
    ContactSpamView, SwipeRefreshLayout.OnRefreshListener {

    companion object {
        fun newInstance(): ContactSpamFragment {
            return ContactSpamFragment()
        }
    }

    private var isViewed = false
    private val contactsSpam by lazy { ArrayList<ContactModel>() }
    private val adapter by lazy {
        ContactSpamAdapter(contactsSpam,
            showInfoListener = {
                ctx?.run {
                    ContactDetailActivity.start(this, it)
                }
            },
            sendMessageListener = {
                // Open SMS intent
                CommonUtil.composeSms(parentActivity, it.number)
            },
            removeSpamListener = {
                ctx?.currentUser?.phoneNumberNonPrefix?.run {
                    presenter.removeSpam(this, it)
                }
            })
    }

    override fun onResume() {
        super.onResume()

        if (!isViewed) {
            getSpamList(true)
            isViewed = true
        }
    }

    override fun initView(): ContactSpamView {
        return this
    }

    override fun initPresenter(): ContactSpamPresenterImp {
        return ContactSpamPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contact_spam
    }

    override fun initWidgets() {
        swpContactSpam.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
        swpContactSpam.setOnRefreshListener(this)
        rclContactSpam.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclContactSpam.setHasFixedSize(true)
        rclContactSpam.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        rclContactSpam.adapter = adapter
    }

    override fun onRefresh() {
        getSpamList(false)
    }

    override fun onContactsSpamLoaded(contacts: List<ContactModel>) {
        if (contacts.isNotEmpty()) {
            contactsSpam.clear()
            contactsSpam.addAll(contacts)
            adapter.notifyDataSetChanged()
        }

        // Show/hide 'no data' layout
        toggleNoDataLayout()
    }

    override fun onRemoveSpamSuccess(contact: ContactModel) {
        val removedIndex = contactsSpam.indexOfFirst {
            it.number == contact.number
        }
        if (removedIndex > -1) {
            contactsSpam.removeAt(removedIndex)
            adapter.notifyItemRemoved(removedIndex)

            // Show/hide 'no data' layout
            toggleNoDataLayout()
        }
    }

    override fun dismissRefreshIcon() {
        swpContactSpam.isRefreshing = false
    }

    private fun getSpamList(showProgress: Boolean) {
        ctx?.currentUser?.phoneNumberNonPrefix?.run {
            presenter.getMySpamList(this, showProgress)
        }
    }

    private fun toggleNoDataLayout() {
        if (contactsSpam.isNotEmpty()) {
            rclContactSpam.visible()
            lblNoData.gone()
        } else {
            rclContactSpam.gone()
            lblNoData.visible()
        }
    }
}
