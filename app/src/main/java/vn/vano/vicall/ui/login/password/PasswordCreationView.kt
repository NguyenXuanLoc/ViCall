package vn.vano.vicall.ui.login.password

import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseView

interface PasswordCreationView : BaseView {

    fun onPasswordCreatedSuccess(userModel: UserModel)

    fun passwordIsValid(): Boolean

    fun onPasswordError()

    fun passwordRetypeIsValid(): Boolean

    fun onPasswordRetypeError()
}