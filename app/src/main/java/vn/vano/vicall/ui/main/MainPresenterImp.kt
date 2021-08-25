package vn.vano.vicall.ui.main

import android.content.Context
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BasePresenterImp

class MainPresenterImp(private val ctx: Context) : BasePresenterImp<MainView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun listenUserInfoChanged() {
        view?.also { v ->
            RxBus.listenUserModel()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onUserInfoChanged(it)
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun openContactAddingDefaultApp() {
        view?.also {
            interactor.openContactAddingDefaultApp(ctx)
        }
    }

    fun getContactsFromLocal() {
        view?.also { v ->
            showProgressDialog()

            interactor.getLocalContacts(ctx) { contacts ->
                // Notify to view
                v.onContactsLoadedSuccess(contacts)
            }
        }
    }

    fun syncContacts(phoneNumber: String, contacts: ArrayList<ContactModel>) {
        view?.also { v ->
            if (networkIsAvailable()) {
                interactor.syncContacts(phoneNumber, contacts)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        { response ->
                            if (response.isNotEmpty()) {
                                // Save user contacts into shared pref
                                CommonUtil.saveUserContacts(ctx, contacts)
                            }

                            // Notify to view
                            v.onContactsSyncedSuccess()

                            dismissProgressDialog()
                        },
                        {
                            v.onApiCallError()
                            dismissProgressDialog()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            }
        }
    }
}