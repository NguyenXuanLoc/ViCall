package vn.vano.vicall.common.util

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import vn.vano.vicall.data.model.*

// Use object so we have a singleton instance
object RxBus {

    private val publisher = PublishSubject.create<Any?>()

    private fun publish(event: Any?) {
        event?.let {
            publisher.onNext(it)
        }
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    private fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

    fun publishUserModel(model: UserModel) {
        publish(model)
    }

    fun listenUserModel(): Observable<UserModel> {
        return listen(UserModel::class.java)
    }

    fun publishCallStateModel(model: CallStateModel) {
        publish(model)
    }

    fun listenCallStateModel(): Observable<CallStateModel> {
        return listen(CallStateModel::class.java)
    }

    fun publishCallHistoryEventModel(model: CallHistoryEventModel) {
        publish(model)
    }

    fun listenCallHistoryEventModel(): Observable<CallHistoryEventModel> {
        return listen(CallHistoryEventModel::class.java)
    }

    fun publishVideoLayoutSwitcherEventModel(model: VideoLayoutSwitcherEventModel) {
        publish(model)
    }

    fun listenVideoLayoutSwitcherEventModel(): Observable<VideoLayoutSwitcherEventModel> {
        return listen(VideoLayoutSwitcherEventModel::class.java)
    }

    fun publishUserOnCallEventModel(model: UserOnCallEventModel) {
        publish(model)
    }

    fun listenUserOnCallEventModel(): Observable<UserOnCallEventModel> {
        return listen(UserOnCallEventModel::class.java)
    }
}