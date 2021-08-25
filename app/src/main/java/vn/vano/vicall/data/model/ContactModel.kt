package vn.vano.vicall.data.model

import android.provider.CallLog
import vn.vano.vicall.common.util.PermissionUtil

data class ContactModel(val contactIdLocal: Long? = null) : BaseModel() {
    var contactIdServer: String? = null
    var callId: Long? = null
    var number: String = ""
    var name: String? = null
    var email: String? = null
    var company: String? = null
    var address: String? = null
    var birthday: Long? = null
    var callType: Int? = null
    var date: Long? = null
    var duration: Int? = null
    var avatar: String? = null
    var isSpam: Boolean = false
    var status: String? = null
    var isViCallUser: Boolean = false
    var thumbnailVideo: String? = null
    var urlVideoLocal: String? = null
    var lookUpKey: String? = null
    var lookUpUri: String? = null

    fun isIncomingCall(): Boolean {
        return callType == CallLog.Calls.INCOMING_TYPE
    }

    fun isOutgoingCall(): Boolean {
        return callType == CallLog.Calls.OUTGOING_TYPE
    }

    fun isMissedCall(): Boolean {
        return callType == CallLog.Calls.MISSED_TYPE
    }

    fun isRejectedCall(): Boolean {
        return if (PermissionUtil.isApi24orHigher()) {
            callType == CallLog.Calls.REJECTED_TYPE
        } else {
            callType == 5 // Use this constant for api 23 (It's not working on some devices)
        }
    }

    fun isBlockedCall(): Boolean {
        return if (PermissionUtil.isApi24orHigher()) {
            callType == CallLog.Calls.BLOCKED_TYPE
        } else {
            callType == 6 // Use this constant for api 23 (It's not working on some devices)
        }
    }
}