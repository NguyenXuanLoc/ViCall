package vn.vano.vicall.ui.searchingcontact

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface SearchingContactView : BaseView {

    fun onTypingSearch(query: String)

    fun onContactsLoadedSuccess(contacts: List<ContactModel>)
}