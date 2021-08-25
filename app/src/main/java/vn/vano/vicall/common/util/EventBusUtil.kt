package vn.vano.vicall.common.util

import android.content.Context
import org.greenrobot.eventbus.EventBus

object EventBusUtil {
    fun senDataSticky(data: Any) {
        EventBus.getDefault().postSticky(data)
    }

    fun senDataNotSticky(data: Any) {
        EventBus.getDefault().post(data)
    }

    fun register(ctx: Context) {
        EventBus.getDefault().register(ctx)
    }

    fun unRegister(ctx: Context) {
        EventBus.getDefault().unregister(ctx)
    }

    fun removeAllSticky() {
        EventBus.getDefault().removeAllStickyEvents()
    }
}