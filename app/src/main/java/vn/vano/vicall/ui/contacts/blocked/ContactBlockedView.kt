package vn.vano.vicall.ui.contacts.blocked

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface ContactBlockedView : BaseView {

    fun onContactsBlockedLoaded(contacts: List<ContactModel>)

    fun onUnblockedSuccess(contact: ContactModel)
}