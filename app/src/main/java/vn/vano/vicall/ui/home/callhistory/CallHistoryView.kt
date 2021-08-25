package vn.vano.vicall.ui.home.callhistory

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface CallHistoryView : BaseView {

    fun onSpamListLoadedSuccess(spamList: List<ContactModel>)

    fun onCallHistoryLoadedSuccess(calls: ArrayList<ContactModel>)

    fun onNewCallAdded(call: ContactModel)

    fun onReportedSpamSuccess(spamCall: ContactModel)
}