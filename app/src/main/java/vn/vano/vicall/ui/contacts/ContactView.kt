package vn.vano.vicall.ui.contacts

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface ContactView : BaseView {

    fun onContactsLoadedSuccess(contacts: ArrayList<ContactModel>)

    fun onContactsSyncedSuccess()
}