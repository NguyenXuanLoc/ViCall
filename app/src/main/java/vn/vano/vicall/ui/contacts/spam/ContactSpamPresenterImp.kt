package vn.vano.vicall.ui.contacts.spam

import android.content.Context
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BasePresenterImp

class ContactSpamPresenterImp(ctx: Context) : BasePresenterImp<ContactSpamView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun getMySpamList(phoneNumber: String, showProgress: Boolean) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (showProgress) {
                    showProgressDialog()
                }

                interactor.getServerContacts(phoneNumber, Constant.SPAM)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.onContactsSpamLoaded(it)
                            v.dismissRefreshIcon()
                            dismissProgressDialog()
                        },
                        {
                            v.onApiCallError()
                            v.dismissRefreshIcon()
                            dismissProgressDialog()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            }
        }
    }

    fun removeSpam(myNumber: String, contact: ContactModel) {
        view?.also { v ->
            if (networkIsAvailable()) {
                showProgressDialog()

                interactor.markAsMySpam(myNumber, contact.number, Constant.UNMARK)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            if (it.responseIsSuccess()) {
                                v.onRemoveSpamSuccess(contact)
                            } else {
                                v.onApiResponseError(it)
                            }

                            dismissProgressDialog()
                        },
                        {
                            v.onApiCallError()
                            dismissProgressDialog()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }
}