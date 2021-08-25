package vn.vano.vicall.ui.notification

import android.content.Context
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.addToCompositeDisposable
import vn.vano.vicall.common.ext.applyIOWithAndroidMainThread
import vn.vano.vicall.common.ext.networkIsConnected
import vn.vano.vicall.data.interactor.ActivityInteractor
import vn.vano.vicall.ui.base.BasePresenterImp

class NotificationsPresenterImp(val ctx: Context) : BasePresenterImp<NotificationsView>(ctx) {
    private val interactor by lazy { ActivityInteractor() }

    fun getAnnouncement(phoneNumber: String, page: String, size: String, check: Boolean) {
        view?.also { v ->
            if (ctx.networkIsConnected()) {
                if (check) showProgressDialog()
                interactor.getActivities(Constant.NOTIFY, phoneNumber, page, size)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.onAnnouncementLoaded(it)
                            if (it.isEmpty()) v.checkNull(true)
                            dismissProgressDialog()
                            v.dismissRefreshIcon()
                        },
                        {
                            v.onApiCallError()
                            dismissProgressDialog()
                            v.dismissRefreshIcon()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.dismissRefreshIcon()
                v.onNetworkError()
            }
        }
    }

    fun readNotify(id: String) {
        view?.also { v ->
            if (ctx.networkIsConnected()) {
                interactor.readActivity(id)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.onNotificationMarkedReadSuccess()
                        },
                        {
                            v.onApiCallError()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }

    fun readNotifyAll(phoneNumber: String) {
        view?.also { v ->
            if (ctx.networkIsConnected()) {
                interactor.readNotifyAll(Key.ALL, phoneNumber)
                    .applyIOWithAndroidMainThread()
                    .subscribe(
                        {
                            v.readNotifyAllSuccess()
                        },
                        {
                            v.onApiCallError()
                        }
                    ).addToCompositeDisposable(compositeDisposable)
            } else {
                v.onNetworkError()
            }
        }
    }
}