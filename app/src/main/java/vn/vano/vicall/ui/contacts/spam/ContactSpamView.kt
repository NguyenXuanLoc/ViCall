package vn.vano.vicall.ui.contacts.spam

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface ContactSpamView : BaseView {

    fun onContactsSpamLoaded(contacts: List<ContactModel>)

    fun onRemoveSpamSuccess(contact: ContactModel)
}