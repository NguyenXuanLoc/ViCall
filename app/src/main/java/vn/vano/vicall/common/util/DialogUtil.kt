package vn.vano.vicall.common.util


import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.TextView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.visible

object DialogUtil {

    fun showAlert(
        context: Context?,
        textMessage: Any,
        textTitle: Any? = null,
        textOk: Any = context?.getString(R.string.ok) ?: "",
        textCancel: Any? = null,
        cancelable: Boolean = true,
        okListener: (() -> Unit)? = null,
        cancelListener: (() -> Unit)? = null
    ) {
        context?.run {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window?.setBackgroundDrawableResource(R.color.black)
            dialog.setContentView(R.layout.dialog_confirmation)
            dialog.setCancelable(cancelable)

            val lblTitle = dialog.findViewById<TextView>(R.id.lblTitle)
            val lblMessage = dialog.findViewById<TextView>(R.id.lblMessage)
            val lblOk = dialog.findViewById<TextView>(R.id.lblOk)
            val lblCancel = dialog.findViewById<TextView>(R.id.lblCancel)

            textTitle?.let {
                lblTitle.visible()
                lblTitle.text = when (it) {
                    is String -> it
                    is CharSequence -> it
                    is Int -> context.getString(it)
                    else -> ""
                }
            }

            lblMessage.text = when (textMessage) {
                is String -> textMessage
                is CharSequence -> textMessage
                is Int -> context.getString(textMessage)
                else -> ""
            }

            lblOk.text = when (textOk) {
                is String -> textOk
                is CharSequence -> textOk
                is Int -> context.getString(textOk)
                else -> ""
            }
            lblOk.setOnSafeClickListener {
                if (dialog.isShowing) {
                    dialog.dismiss()
                    okListener?.invoke()
                }
            }

            val strCancel = when (textCancel) {
                is String -> textCancel
                is CharSequence -> textCancel
                is Int -> context.getString(textCancel)
                else -> ""
            }
            if (strCancel.isEmpty() || strCancel.isBlank()) {
                lblCancel.visibility = View.GONE
            } else {
                lblCancel.text = strCancel
                lblCancel.setOnSafeClickListener {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        cancelListener?.invoke()
                    }
                }
            }

            if (!dialog.isShowing) {
                dialog.show()
            }
        }
    }
}