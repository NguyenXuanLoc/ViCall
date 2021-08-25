package vn.vano.vicall.ui.contacts.allcontact

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_contact_all.*
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity
import vn.vano.vicall.widget.PaginationScrollListener

class ContactAllFragment : BaseFragment<ContactAllView, ContactAllPresenterImp>(), ContactAllView,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private const val RC_PERMISSION_CALL_PHONE = 1

        fun newInstance(): ContactAllFragment {
            return ContactAllFragment()
        }
    }

    private var clickedContactModel: ContactModel? = null
    private val allContacts by lazy { ArrayList<ContactModel>() }
    private val adapter by lazy {
        ContactAllAdapter(allContacts,
            showInfoListener = {
                ctx?.run {
                    ContactDetailActivity.start(this, it)
                }
            },
            sendMessageListener = {
                // Open SMS intent
                CommonUtil.composeSms(parentActivity, it.number)
            },
            itemClickListener = {
                call(it)
            })
    }
    private var currentPage = 1
    private var hasMoreData = true
    private var isLoading = false
    private val paginationScrollListener by lazy {
        object : PaginationScrollListener(rclContacts.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                rclContacts.post {
                    adapter.addLoadingView()
                }
                getContacts(false)
            }

            override fun isLastPage(): Boolean {
                return !hasMoreData
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get contacts from local
        getContacts(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_CALL_PHONE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            call(clickedContactModel)
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onDestroyView() {
        rclContacts.removeOnScrollListener(paginationScrollListener)
        super.onDestroyView()
    }

    override fun initView(): ContactAllView {
        return this
    }

    override fun initPresenter(): ContactAllPresenterImp {
        return ContactAllPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_contact_all
    }

    override fun initWidgets() {
        swpContacts.setOnRefreshListener(this)
        swpContacts.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )

        rclContacts.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclContacts.setHasFixedSize(true)
        rclContacts.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        rclContacts.adapter = adapter
        rclContacts.addOnScrollListener(paginationScrollListener)
    }

    override fun onContactsLoadedSuccess(contacts: ArrayList<ContactModel>) {
        // Refresh adapter
        if (currentPage == 1) { // Reset old data if current page = 1(the first call or user refreshes the list)
            allContacts.clear()
            allContacts.addAll(contacts)
            adapter.notifyDataSetChanged()
        } else {
            rclContacts.post {
                adapter.removeLoadingView()
            }
            allContacts.addAll(contacts)
            adapter.notifyItemRangeInserted(allContacts.size - contacts.size, contacts.size)
        }

        // Update pagination flags
        hasMoreData = contacts.size == Constant.PAGE_SIZE
        isLoading = false

        // Show/hide 'no data' layout
        toggleNoDataLayout()

        // Sync contacts to server
        /*ctx?.currentUser?.phoneNumberNonPrefix?.run {
            if (contacts.isNotEmpty()) {
                presenter.syncContacts(this, contacts)
            }
        }*/
    }

    override fun onRefresh() {
        currentPage = 1
        hasMoreData = false
        getContacts(false)
    }

    override fun dismissRefreshIcon() {
        swpContacts.isRefreshing = false
    }

    private fun toggleNoDataLayout() {
        if (allContacts.isNotEmpty()) {
            rclContacts.visible()
            lblNoData.gone()
        } else {
            rclContacts.gone()
            lblNoData.visible()
        }
    }

    private fun call(contact: ContactModel?) {
        if (PermissionUtil.isGranted(
                self,
                arrayOf(Manifest.permission.CALL_PHONE),
                RC_PERMISSION_CALL_PHONE
            )
        ) {
            clickedContactModel = contact
            CommonUtil.call(parentActivity, clickedContactModel?.number)
        }
    }

    private fun getContacts(showProgress: Boolean) {
        presenter.getContactsFromLocal(showProgress, currentPage)
    }
}
