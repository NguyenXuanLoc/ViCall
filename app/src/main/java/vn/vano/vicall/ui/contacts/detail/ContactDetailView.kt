package vn.vano.vicall.ui.contacts.detail

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface ContactDetailView : BaseView {
    fun onCallDetailLoaded(call: ContactModel)

    fun onReportedSpamSuccess()

    fun onUnblockedSuccess(contact: ContactModel)
}