package vn.vano.vicall.ui.callerid

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import org.jetbrains.anko.ctx
import vn.vano.vicall.common.CallDirection
import vn.vano.vicall.common.ext.isBlocked
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil

@RequiresApi(Build.VERSION_CODES.Q)
class CallScreeningServiceImp : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        if (PermissionUtil.isApi29orHigher()) {
            val callDirection = when (callDetails.callDirection) {
                Call.Details.DIRECTION_INCOMING -> CallDirection.INCOMING
                Call.Details.DIRECTION_OUTGOING -> CallDirection.OUTGOING
                else -> null
            }
            callDirection?.run {
                val phoneNumber = callDetails.handle?.schemeSpecificPart
                phoneNumber?.also { number ->
                    val isBlocked = ctx.isBlocked(number)
                    if (!isBlocked) {
                        val shouldShowCallWindow = if (callDirection == CallDirection.INCOMING) {
                            true
                        } else {
                            val contact = CommonUtil.getUserContacts(ctx).find {
                                it.number.removeSpaces()
                                    .removePhonePrefix() == number.removeSpaces()
                                    .removePhonePrefix()
                            }

                            val hasInContact = contact != null
                            val hasStatus = contact?.status?.isNotBlank() == true
                            !hasInContact || (hasInContact && hasStatus)
                        }

                        if (shouldShowCallWindow) {
                            CallActivity.newInstance(ctx, number, callDirection)
                        }
                    }
                }
            }
        }
    }
}