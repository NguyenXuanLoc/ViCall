package vn.vano.vicall.ui.registerservice

import vn.vano.vicall.data.model.RegisterModel
import vn.vano.vicall.ui.base.BaseView

interface RegisterServiceView : BaseView {
    fun configLoaded(registerModel: RegisterModel)
}

