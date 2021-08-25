package vn.vano.vicall.ui.home.spamcall

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface CallSpamView : BaseView {

    fun onCallHistoryLoaded(callHistory: List<ContactModel>)
}