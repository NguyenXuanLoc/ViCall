package vn.vano.vicall.ui.callerid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import vn.vano.vicall.common.CallDirection
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.PermissionUtil

class OutgoingCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            context?.run {
                if (!PermissionUtil.isApi29orHigher()) {
                    val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                    showOutgoingUI(this, phoneNumber)
                }
            }
        }
    }

    private fun showOutgoingUI(ctx: Context, phoneNumber: String?) {
        phoneNumber?.also { number ->
            if (!number.trim().startsWith("*")) {
                val contact = CommonUtil.getUserContacts(ctx).find {
                    it.number.removeSpaces().removePhonePrefix() == number.removeSpaces()
                        .removePhonePrefix()
                }

                val hasInContact = contact != null
                val hasStatus = contact?.status?.isNotBlank() == true
                val shouldShowOutgoingWindow = !hasInContact || (hasInContact && hasStatus)

                if (shouldShowOutgoingWindow) {
                    CallActivity.newInstance(ctx, number, CallDirection.OUTGOING)
                }
            }
        }
    }
}