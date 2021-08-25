package vn.vano.vicall.ui.main

import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseView

interface MainView : BaseView {

    fun onUserInfoChanged(userModel: UserModel)

    fun onContactsLoadedSuccess(contacts: ArrayList<ContactModel>)

    fun onContactsSyncedSuccess()
}