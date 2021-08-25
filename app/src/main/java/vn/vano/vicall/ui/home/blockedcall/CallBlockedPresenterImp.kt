package vn.vano.vicall.ui.home.blockedcall

import android.content.Context
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.ui.base.BasePresenterImp

class CallBlockedPresenterImp(private val ctx: Context) : BasePresenterImp<CallBlockedView>(ctx) {

    fun listenCallHistoryChanged() {
        view?.also { v ->
            RxBus.listenCallHistoryEventModel()
                .applyIOWithAndroidMainThread()
                .subscribe(
                    {
                        v.onCallHistoryLoaded(it.calls)
                    },
                    {
                    }
                ).addToCompositeDisposable(compositeDisposable)
        }
    }
}