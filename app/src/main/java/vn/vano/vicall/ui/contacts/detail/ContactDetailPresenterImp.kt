package vn.vano.vicall.ui.contacts.detail

import android.content.Context
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.unblockNumber
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BasePresenterImp

class ContactDetailPresenterImp(private val ctx: Context) :
    BasePresenterImp<ContactDetailView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun reportSpam(myNumber: String, spamNumber: String) {
        view?.also { v ->
            if (networkIsAvailable()) {
                showProgressDialog()

                interactor.markAsMySpam(myNumber, spamNumber, Constant.MARK)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            if (it.responseIsSuccess()) {
                                v.onReportedSpamSuccess()
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

    fun getContactDetailFromLookUpKey(lookUpKey: String?, updatedContact: (ContactModel) -> Unit) {
        view?.also { v ->
            interactor.getContactsDetailFromLookUpKey(ctx, lookUpKey) {
                updatedContact(it)
            }
        }
    }

    fun getCallDetailByPhoneNumber(number: String) {
        view?.also { v ->
            interactor.getCallLogByPhoneNumber(ctx, number) {
                v.onCallDetailLoaded(it)
            }
        }
    }

    fun openContactEditingDefaultApp(lookUpUri: String, requestCode: Int) {
        view?.also {
            interactor.openContactEditingDefaultApp(ctx, lookUpUri, requestCode)
        }
    }

    fun openContactAddingDefaultApp(number: String) {
        view?.also {
            interactor.openContactAddingDefaultApp(ctx, number)
        }
    }

    fun unblockContact(contact: ContactModel) {
        view?.also { v ->
            if (PermissionUtil.isApi24orHigher()) {
                if (ctx.unblockNumber(contact.number)) {
                    v.onUnblockedSuccess(contact)
                }
            }
        }
    }
}