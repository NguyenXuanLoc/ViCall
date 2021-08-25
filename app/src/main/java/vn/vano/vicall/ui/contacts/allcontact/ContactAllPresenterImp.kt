package vn.vano.vicall.ui.contacts.allcontact

import android.content.Context
import io.reactivex.schedulers.Schedulers
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BasePresenterImp

class ContactAllPresenterImp(private val ctx: Context) : BasePresenterImp<ContactAllView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun getContactsFromLocal(showProgress: Boolean, page: Int) {
        view?.also { v ->
            if (showProgress) {
                showProgressDialog()
            }

            interactor.getLocalContacts(ctx, page = page) { contacts ->
                // Map saved fields with local fields
                val savedContacts = CommonUtil.getUserContacts(ctx)
                if (savedContacts.isNotEmpty()) {
                    for (contact in contacts) {
                        savedContacts.find {
                            contact.number.removeSpaces()
                                .removePhonePrefix() == it.number.removeSpaces()
                                .removePhonePrefix()
                        }?.run {
                            contact.status = status
                            contact.isViCallUser = isViCallUser
                        }
                    }
                }

                // Notify to view
                v.onContactsLoadedSuccess(contacts)
                dismissProgressDialog()
                v.dismissRefreshIcon()

                // Save user contacts into shared pref
                CommonUtil.saveUserContacts(ctx, contacts)
            }
        }
    }

    fun syncContacts(phoneNumber: String, contacts: ArrayList<ContactModel>) {
        view?.also { v ->
            if (networkIsAvailable()) {
                interactor.syncContacts(phoneNumber, contacts)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { response ->
                            // Save user contacts into shared pref
                            if (response.isNotEmpty()) {
                                val mergedContacts = arrayListOf<ContactModel>()
                                CommonUtil.getUserContacts(ctx).map { contact ->
                                    for (res in response) {
                                        if (contact.number.removeSpaces()
                                                .removePhonePrefix() == res.number.removeSpaces()
                                                .removePhonePrefix()
                                        ) {
                                            contact.avatar = contact.avatar ?: res.avatar
                                            contact.name = contact.name ?: res.name
                                            contact.status = res.status
                                            contact.isViCallUser = res.isViCallUser

                                            mergedContacts.add(contact)
                                        }
                                    }
                                    CommonUtil.saveUserContacts(ctx, mergedContacts)
                                }
                            }
                        },
                        {
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            }
        }
    }
}