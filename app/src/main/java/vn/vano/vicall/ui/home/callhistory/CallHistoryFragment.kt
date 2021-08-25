package vn.vano.vicall.ui.home.callhistory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_call_history.*
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.model.CallHistoryEventModel
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity
import vn.vano.vicall.ui.home.spamcall.CallSpamFragment
import vn.vano.vicall.widget.PaginationScrollListener

class CallHistoryFragment : BaseFragment<CallHistoryView, CallHistoryPresenterImp>(),
    CallHistoryView {

    companion object {
        private const val RC_PERMISSION_WRITE_CALL_LOG = 1
        private const val RC_PERMISSION_CALL_PHONE = 2

        fun newInstance(): CallHistoryFragment {
            return CallHistoryFragment()
        }
    }

    private var clickedContactModel: ContactModel? = null
    private val spamList by lazy { arrayListOf<ContactModel>() }
    private val calls by lazy { ArrayList<ContactModel>() }
    private val adapter by lazy {
        CallHistoryAdapter(calls,
            showInfoListener = {
                ctx?.run {
                    ContactDetailActivity.start(this, it)
                }
            },
            sendMessageListener = {
                // Open SMS intent
                CommonUtil.composeSms(parentActivity, it.number)
            },
            reportSpamListener = {
                ctx?.currentUser?.phoneNumberNonPrefix?.run {
                    presenter.reportSpam(this, it)
                }
            },
            itemClickListener = {
                call(it)
            })
    }
    private var currentPage = 1
    private var hasMoreData = true
    private var isLoading = false
    private val paginationScrollListener by lazy {
        object : PaginationScrollListener(rclCallHistory.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                rclCallHistory.post {
                    adapter.addLoadingView()
                }
                getCallHistory(false)
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

        // Get call log
        if (ctx?.networkIsConnected() == true) {
            ctx?.currentUser?.phoneNumberNonPrefix?.run {
                presenter.getViCallSpamList(this)
            }
        } else {
            getCallHistory()
        }
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
        rclCallHistory.removeOnScrollListener(paginationScrollListener)
        super.onDestroyView()
    }

    override fun initView(): CallHistoryView {
        return this
    }

    override fun initPresenter(): CallHistoryPresenterImp {
        return CallHistoryPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_call_history
    }

    override fun initWidgets() {
        rclCallHistory.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclCallHistory.setHasFixedSize(true)
        rclCallHistory.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        rclCallHistory.adapter = adapter
        rclCallHistory.addOnScrollListener(paginationScrollListener)

        // Listeners
        presenter.listenNewCallAdded(self)
    }

    override fun onSpamListLoadedSuccess(spamList: List<ContactModel>) {
        this.spamList.addAll(spamList)
        getCallHistory()
    }

    override fun onCallHistoryLoadedSuccess(calls: ArrayList<ContactModel>) {
        // Map with spam list
        if (spamList.isNotEmpty()) {
            for (spam in spamList) {
                calls.filter {
                    it.number.removePhonePrefix() == spam.number.removePhonePrefix()
                }.map {
                    it.isSpam = true
                }
            }
        }

        // Push call history changed event
        publishCallHistoryChanged(calls)

        // Remove loadmore view
        if (currentPage > 1) {
            if (calls.size != 0 && calls.size % Constant.PAGE_SIZE == 0) {
                adapter.removeLoadingView()
            } else {
                rclCallHistory.post {
                    adapter.removeLoadingView()
                }
            }
        }

        // Add to the list and refresh the adapter
        this.calls.addAll(calls)
        adapter.notifyItemRangeInserted(this.calls.size - calls.size, calls.size)

        // Update pagination flags
        hasMoreData = calls.size == Constant.PAGE_SIZE
        isLoading = false

        // Show/hide 'no data' layout
        toggleNoDataLayout()
    }

    override fun onNewCallAdded(call: ContactModel) {
        if (calls.isNotEmpty()) {
            calls.find {
                it.callId == call.callId
            }?.run {
                // Do nothing because of it's duplicated item
            } ?: run {
                // Map with spam list
                if (spamList.isNotEmpty()) {
                    spamList.find {
                        it.number.removePhonePrefix() == call.number.removePhonePrefix()
                    }?.run {
                        // Assign the added call is spam
                        call.isSpam = true
                    }
                }

                // Push call history changed event
                publishCallHistoryChanged(listOf(call))

                // Add to the list and refresh the adapter
                this.calls.add(0, call)
                adapter.notifyItemInserted(0)
            }
        }
    }

    override fun onReportedSpamSuccess(spamCall: ContactModel) {
        ctx?.toast(R.string.reported_spam_success)
        publishCallHistoryChanged(listOf(spamCall))
    }

    fun notifyDeletedCall(call: ContactModel) {
        val deletedIndex = calls.indexOfFirst { it.callId == call.callId }
        if (deletedIndex > -1) {
            calls.removeAt(deletedIndex)
            adapter.notifyItemRemoved(deletedIndex)

            // Show/hide 'no data' layout
            toggleNoDataLayout()
        }
    }

    private fun getCallHistory(showProgress: Boolean = true) {
        presenter.getCallLog(currentPage, showProgress)
    }

    private fun publishCallHistoryChanged(spamList: List<ContactModel>) {
        RxBus.publishCallHistoryEventModel(CallHistoryEventModel(spamList))
    }

    private fun toggleNoDataLayout() {
        if (calls.isNotEmpty()) {
            rclCallHistory.visible()
            lblNoData.gone()
        } else {
            rclCallHistory.gone()
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
}
