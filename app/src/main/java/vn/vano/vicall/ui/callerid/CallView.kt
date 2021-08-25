package vn.vano.vicall.ui.callerid

import vn.vano.vicall.common.CallState
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseView

interface CallView : BaseView {
    fun onCallStateChanged(state: CallState)
    fun onGettingLocalUserOnCallSuccess(userModel: UserModel?)
    fun onGettingServerUserOnCallSuccess(userModel: UserModel)
}