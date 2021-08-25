package vn.vano.vicall.ui.login

import vn.vano.vicall.data.model.OtpModel
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseView

interface LoginView : BaseView {

    fun onLoginSuccess(userModel: UserModel)

    fun onTypingPhoneNumber(number: String)

    fun onGettingUserByPhoneSuccess(userModel: UserModel?)

    fun phoneNumberIsValid(): Boolean

    fun onPhoneNumberError()

    fun passwordIsValid(): Boolean

    fun onPasswordError()

    fun onGetOtpSuccess(otpModel: OtpModel, phoneNumber: String)

    fun onGetDeviceTokenSuccess(token: String)
}