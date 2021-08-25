package vn.vano.vicall.ui.home.blockedcall

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface CallBlockedView : BaseView {

    fun onCallHistoryLoaded(callHistory: List<ContactModel>)
}