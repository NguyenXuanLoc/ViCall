package vn.vano.vicall.ui.contacts.allcontact

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface ContactAllView : BaseView {

    fun onContactsLoadedSuccess(contacts: ArrayList<ContactModel>)
}