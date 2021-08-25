package vn.vano.vicall.ui.dialer

import android.content.Context
import android.widget.TextView
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.RxSearchObservable
import vn.vano.vicall.data.interactor.ContactInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class DialerPresenterImp(val ctx: Context) : BasePresenterImp<DialerView>(ctx) {

    private val interactor by lazy { ContactInteractor() }

    fun searchContacts(query: String) {
        view?.also { v ->
            interactor.getLocalContacts(ctx, query, 20) {
                v.onContactsLoadedSuccess(it)
            }
        }
    }

    fun listenDialNumberChanged(dialLabel: TextView) {
        view?.let { v ->
            RxSearchObservable.fromView(dialLabel)
                .applyIOWithAndroidMainThread()
                .subscribe {
                    v.onDialNumberChanged(it)
                }.addToCompositeDisposable(compositeDisposable)
        }
    }

    fun openContactAddingDefaultApp(number: String) {
        view?.also {
            interactor.openContactAddingDefaultApp(ctx, number)
        }
    }

    fun getLastCall() {
        view?.also { v ->
            interactor.getLastCallLog(ctx) {
                v.onGetLastCallSuccess(it)
            }
        }
    }
}