package vn.vano.vicall.ui.login.loggedinlist

import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseView

interface LoggedInListView : BaseView {

    fun onUsersLoadedSuccess(users: List<UserModel>)
}