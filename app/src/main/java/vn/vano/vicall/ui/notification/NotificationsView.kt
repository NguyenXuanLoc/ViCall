package vn.vano.vicall.ui.notification

import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.ui.base.BaseView

interface NotificationsView : BaseView {

    fun onAnnouncementLoaded(result: List<ActivityModel>)
    fun checkNull(boolean: Boolean)
    fun onNotificationMarkedReadSuccess()
    fun readNotifyAllSuccess()
}