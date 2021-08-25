package vn.vano.vicall.ui.contacts.insertcontact

import vn.vano.vicall.ui.base.BaseView

interface InsertContactView : BaseView {

    fun onContactAddedSuccess()

    fun nameIsValid() : Boolean

    fun onNameError()

    fun phoneIsValid() : Boolean

    fun onPhoneError()
}