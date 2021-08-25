package vn.vano.vicall.ui.home.callhistory

import android.content.Context
import androidx.fragment.app.Fragment
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.ui.base.BasePresenterImp

class CallHistoryPresenterImp(private val ctx: Context) : BasePresenterImp<CallHistoryView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun listenNewCallAdded(fragment: Fragment) {
        view?.also { v ->
            interactor.listenCallLogChanged(ctx, fragment) { call ->
                // Map with saved contacts
                val savedContacts = CommonUtil.getUserContacts(ctx)
                if (savedContacts.isNotEmpty()) {
                    val contact = savedContacts.find {
                        call.number.removeSpaces()
                            .removePhonePrefix() == it.number.removeSpaces()
                            .removePhonePrefix()
                    }

                    val calledUser = CommonUtil.getCallerByPhone(ctx, call.number)
                    call.avatar = contact?.avatar ?: call.avatar ?: calledUser?.avatar
                    call.name = contact?.name ?: call.name ?: calledUser?.name
                    call.status = contact?.status ?: calledUser?.status
                    call.lookUpUri = contact?.lookUpUri
                    call.isViCallUser = calledUser?.name?.isNotBlank() == true
                }

                // Notify to view
                v.onNewCallAdded(call)
            }
        }
    }

    fun getCallLog(page: Int, showProgress: Boolean) {
        view?.also { v ->
            if (showProgress) {
                showProgressDialog()
            }

            interactor.getCallLog(ctx, page) { calls ->
                if (calls.isNotEmpty()) {
                    // Map with saved contacts
                    val savedContacts = CommonUtil.getUserContacts(ctx)
                    if (savedContacts.isNotEmpty()) {
                        for (call in calls) {
                            val contact = savedContacts.find {
                                call.number.removeSpaces()
                                    .removePhonePrefix() == it.number.removeSpaces()
                                    .removePhonePrefix()
                            }

                            val calledUser = CommonUtil.getCallerByPhone(ctx, call.number)
                            call.avatar = contact?.avatar ?: call.avatar ?: calledUser?.avatar
                            call.name = contact?.name ?: call.name ?: calledUser?.name
                            call.status = contact?.status ?: calledUser?.status
                            call.lookUpUri = contact?.lookUpUri
                            call.isViCallUser = calledUser?.name?.isNotBlank() == true
                        }
                    }
                }

                // Notify to the view
                v.onCallHistoryLoadedSuccess(calls)

                dismissProgressDialog()
            }
        }
    }

    fun getViCallSpamList(phoneNumber: String) {
        view?.also { v ->
            if (networkIsAvailable()) {
                showProgressDialog()

                interactor.getViCallSpamList(phoneNumber)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.onSpamListLoadedSuccess(it)
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

    fun reportSpam(myNumber: String, spamCall: ContactModel) {
        view?.also { v ->
            if (networkIsAvailable()) {
                showProgressDialog()

                interactor.markAsMySpam(myNumber, spamCall.number, Constant.MARK)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            if (it.responseIsSuccess()) {
                                v.onReportedSpamSuccess(
                                    spamCall.apply { isSpam = true }
                                )
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