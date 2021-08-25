package vn.vano.vicall.ui.home.blockedcall

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_call_blocked.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.contacts.detail.ContactDetailActivity

class CallBlockedFragment : BaseFragment<CallBlockedView, CallBlockedPresenterImp>(),
    CallBlockedView {

    companion object {
        fun newInstance(): CallBlockedFragment {
            return CallBlockedFragment()
        }
    }

    private val calls by lazy { ArrayList<ContactModel>() }
    private val adapter by lazy {
        CallBlockedAdapter(calls,
            showInfoListener = {
                ctx?.run {
                    ContactDetailActivity.start(this, it)
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Listen call history change
        presenter.listenCallHistoryChanged()
    }

    override fun initView(): CallBlockedView {
        return this
    }

    override fun initPresenter(): CallBlockedPresenterImp {
        return CallBlockedPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_call_blocked
    }

    override fun initWidgets() {
        // Init UI
        rclCallBlocked.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclCallBlocked.setHasFixedSize(true)
        rclCallBlocked.addItemDecoration(DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL))
        rclCallBlocked.adapter = adapter
    }

    override fun onCallHistoryLoaded(callHistory: List<ContactModel>) {
        // Filter blocked calls from call history
        val blockedCalls = callHistory.filter {
            it.isBlockedCall()
        }
        if (blockedCalls.isNotEmpty()) {
            calls.addAll(blockedCalls)
            calls.sortByDescending {
                it.date
            }
            adapter.notifyDataSetChanged()
        }

        // Show/hide 'No data' layout
        toggleNoDataLayout()
    }

    private fun toggleNoDataLayout() {
        if (calls.isNotEmpty()) {
            rclCallBlocked.visible()
            lblNoData.gone()
        } else {
            rclCallBlocked.gone()
            lblNoData.visible()
        }
    }
}
