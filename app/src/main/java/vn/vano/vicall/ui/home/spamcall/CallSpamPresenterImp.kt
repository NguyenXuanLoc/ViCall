package vn.vano.vicall.ui.home.spamcall

import android.content.Context
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.ui.base.BasePresenterImp

class CallSpamPresenterImp(ctx: Context) : BasePresenterImp<CallSpamView>(ctx) {

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