package vn.vano.vicall.ui.login.registration

import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseView

interface RegistrationView : BaseView {

    fun onRegistrationSuccess(userModel: UserModel)

    fun onUpdateProfileSuccess(userModel: UserModel)

    fun nameIsValid(): Boolean

    fun onNameError()

    fun deleteTempAvt()
}