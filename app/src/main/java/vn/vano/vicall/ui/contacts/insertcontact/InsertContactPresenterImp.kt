package vn.vano.vicall.ui.contacts.insertcontact

import android.content.Context
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class InsertContactPresenterImp(ctx: Context) : BasePresenterImp<InsertContactView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun addContact(
        ctx: Context,
        name: String,
        phoneNumber: String,
        email: String? = null,
        company: String? = null,
        address: String? = null
    ) {
        view?.also { v ->
            if (v.nameIsValid()) {
                if (v.phoneIsValid()) {
                    interactor.addContact(ctx, name, phoneNumber, email, company, address)
                    v.onContactAddedSuccess()
                } else {
                    v.onPhoneError()
                }
            } else {
                v.onNameError()
            }
        }
    }
}