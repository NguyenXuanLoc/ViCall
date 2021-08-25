package vn.vano.vicall.ui.home.spamcall

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_call_spam.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity
import vn.vano.vicall.ui.home.callhistory.CallHistoryFragment

class CallSpamFragment : BaseFragment<CallSpamView, CallSpamPresenterImp>(), CallSpamView {

    companion object {
        private const val RC_PERMISSION_WRITE_CALL_LOG = 1

        fun newInstance(): CallSpamFragment {
            return CallSpamFragment()
        }
    }

    private var deletingCallModel: ContactModel? = null
    private val calls by lazy { ArrayList<ContactModel>() }
    private val adapter by lazy {
        CallSpamAdapter(calls,
            showInfoListener = {
                ctx?.run {
                    ContactDetailActivity.start(this, it)
                }
            },
            sendMessageListener = {
                // Open SMS intent
                CommonUtil.composeSms(parentActivity, it.number)
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Listen call history change
        presenter.listenCallHistoryChanged()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun initView(): CallSpamView {
        return this
    }

    override fun initPresenter(): CallSpamPresenterImp {
        return CallSpamPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_call_spam
    }

    override fun initWidgets() {
        rclCallSpam.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclCallSpam.setHasFixedSize(true)
        rclCallSpam.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        rclCallSpam.adapter = adapter
    }

    override fun onCallHistoryLoaded(callHistory: List<ContactModel>) {
        // Filter spam calls from call history
        val spamCalls = callHistory.filter { it.isSpam }
        if (spamCalls.isNotEmpty()) {
            calls.addAll(spamCalls)
            calls.sortByDescending {
                it.date
            }
            adapter.notifyDataSetChanged()
        }

        // Show/hide 'no data' layout
        toggleNoDataLayout()
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


    private fun toggleNoDataLayout() {
        if (calls.isNotEmpty()) {
            rclCallSpam.visible()
            lblNoData.gone()
        } else {
            rclCallSpam.gone()
            lblNoData.visible()
        }
    }
}
