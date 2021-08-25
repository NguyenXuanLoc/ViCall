package vn.vano.vicall.ui.callerid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.telephony.TelephonyManager
import vn.vano.vicall.common.CallDirection
import vn.vano.vicall.common.CallState
import vn.vano.vicall.common.ext.isBlocked
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.model.CallStateModel

class IncomingCallReceiver : BroadcastReceiver() {

    companion object {
        var wasRinging = false
        var pushedPhoneState = false
        val handlerPhoneState by lazy { Handler() }
        val runnablePhoneState: Runnable by lazy {
            Runnable {
                pushedPhoneState = false
                handlerPhoneState.removeCallbacks(runnablePhoneState)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    context?.run {
                        if (!PermissionUtil.isApi29orHigher()) {
                            val phoneNumber =
                                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                            phoneNumber?.also { number ->
                                showIncomingUI(this, number)
                            }
                        }
                        wasRinging = true
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Publish call state event to update UI
                    if (!pushedPhoneState) {
                        if (wasRinging) {
                            publishCallState(CallState.ACCEPTED)
                        }

                        pushedPhoneState = true
                        handlerPhoneState.postDelayed(runnablePhoneState, 1500)
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    wasRinging = false
                    // Publish call state event to update UI
                    if (!pushedPhoneState) {
                        publishCallState(CallState.IDLE)

                        pushedPhoneState = true
                        handlerPhoneState.postDelayed(runnablePhoneState, 1500)
                    }
                }
            }
        }
    }

    private fun showIncomingUI(ctx: Context, phoneNumber: String) {
        val isBlocked = ctx.isBlocked(phoneNumber)
        if (!isBlocked) {
            CallActivity.newInstance(ctx, phoneNumber, CallDirection.INCOMING)
        }
    }

    private fun publishCallState(state: CallState) {
        RxBus.publishCallStateModel(CallStateModel(state))
    }
}