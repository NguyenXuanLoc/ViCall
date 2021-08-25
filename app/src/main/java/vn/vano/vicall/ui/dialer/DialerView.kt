package vn.vano.vicall.ui.dialer

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BaseView

interface DialerView : BaseView {

    fun onContactsLoadedSuccess(contacts: List<ContactModel>)

    fun onDialNumberChanged(number: String)

    fun onGetLastCallSuccess(call: ContactModel)
}