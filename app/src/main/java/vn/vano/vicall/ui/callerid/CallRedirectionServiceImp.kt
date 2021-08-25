package vn.vano.vicall.ui.callerid

import android.net.Uri
import android.os.Build
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import androidx.annotation.RequiresApi
import org.jetbrains.anko.ctx
import vn.vano.vicall.common.CallDirection
import vn.vano.vicall.common.util.PermissionUtil

@RequiresApi(Build.VERSION_CODES.Q)
class CallRedirectionServiceImp : CallRedirectionService() {

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        /**
         * The implemented {@link CallRedirectionService} calls this method to response a request
         * received via {@link #onPlaceCall(Uri, PhoneAccountHandle, boolean)} to inform Telecom that
         * no changes are required to the outgoing call, and that the call should be placed as-is.
         *
         * <p>This can only be called from implemented
         * {@link #onPlaceCall(Uri, PhoneAccountHandle, boolean)}. The response corresponds to the
         * latest request via {@link #onPlaceCall(Uri, PhoneAccountHandle, boolean)}.
         *
         */
        placeCallUnmodified()

        // Show call layout
        if (PermissionUtil.isApi29orHigher()) {
            val phoneNumber = handle.schemeSpecificPart
            phoneNumber?.also { number ->
                if (!number.trim().startsWith("*")) {
                    // This is just called if the phone number is in user's contact
                    CallActivity.newInstance(ctx, number, CallDirection.OUTGOING)
                }
            }
        }
    }
}