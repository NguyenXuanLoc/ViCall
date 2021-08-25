package vn.vano.vicall.ui.login.otp

import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseView

interface OtpView : BaseView {

    fun onCountingDown(remainTime: Long)

    fun onOtpVerifiedSuccess(userModel: UserModel)

    fun onUserNotFound()

    fun otpIsValid(): Boolean

    fun onOtpError()
}