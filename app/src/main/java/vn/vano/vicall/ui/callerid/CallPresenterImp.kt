package vn.vano.vicall.ui.callerid

import android.content.Context
import io.reactivex.schedulers.Schedulers
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class CallPresenterImp(val ctx: Context) : BasePresenterImp<CallView>(ctx) {

    private val contactInteractor by lazy { ContactInteractor() }

    fun listenCallStateEvent() {
        view?.also { v ->
            RxBus.listenCallStateModel()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onCallStateChanged(it.state)
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }

    fun getUserOnCallInfo(phoneNumber: String) {
        view?.also { v ->
            v.onGettingLocalUserOnCallSuccess(CommonUtil.getCallerByPhone(ctx, phoneNumber))
        }
    }

    fun openContactAddingDefaultApp(number: String, name: String? = null) {
        view?.also {
            contactInteractor.openContactAddingDefaultApp(ctx, number, name)
        }
    }

    fun openContactEditingDefaultApp(phoneNumber: String) {
        view?.also {
            contactInteractor.getContactsDetailFromNumber(ctx, phoneNumber) {
                it.lookUpUri?.run {
                    contactInteractor.openContactEditingDefaultApp(ctx, this, 256)
                }
            }
        }
    }

    fun reportSpam(myNumber: String, spamNumber: String) {
        view?.also { v ->
            if (networkIsAvailable()) {
                contactInteractor.markAsMySpam(myNumber, spamNumber, Constant.MARK)
                    .subscribeOn(Schedulers.io())
                    .subscribe()
                    .addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun listenUserOnCallEventChange() {
        view?.also { v ->
            RxBus.listenUserOnCallEventModel()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onGettingServerUserOnCallSuccess(it.userModel)
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}