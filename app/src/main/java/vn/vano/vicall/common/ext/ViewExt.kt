package vn.vano.vicall.common.ext

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.snackbar.Snackbar
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.SafeOnClickListener

val View.ctx: Context
    get() = context

var TextView.textColor: Int
    get() = currentTextColor
    set(value) = setTextColor(ContextCompat.getColor(ctx, value))


fun View.slideExit() {
    if (translationY == 0f) animate().translationY(-height.toFloat())
}

fun View.slideEnter() {
    if (translationY < 0f) animate().translationY(0f)
}

fun View.gone() {
    visibility = GONE
}

fun View.visible() {
    visibility = VISIBLE
}

fun View.invisible() {
    visibility = INVISIBLE
}

fun ViewGroup.setAnimation(visibility: Int, animation: Int) {
    if (this.visibility != visibility) {
        val anim = AnimationUtils.loadAnimation(ctx, animation)
        val animController = LayoutAnimationController(anim)

        this.visibility = visibility
        layoutAnimation = animController
        startAnimation(anim)
    }
}

fun View.setRatio(
    activity: AppCompatActivity,
    x: Int,
    y: Int,
    margin: Int,
    width: Int = CommonUtil.getScreenWidthAsPixel(activity)
) {
    val w = width - margin
    layoutParams.width = w
    layoutParams.height = w * y / x
}

fun View.showSnackbar(
    msg: Any?,
    length: Int = Snackbar.LENGTH_SHORT,
    action: Any? = null,
    listener: (() -> Unit)? = null
) {
    val strMsg = when (msg) {
        is CharSequence -> msg
        is String -> msg
        is Int -> ctx.getString(msg)
        else -> "Message format is not supported"
    }
    val snackBar = Snackbar.make(this, strMsg, length)
    if (action != null) {
        val strAction = when (action) {
            is CharSequence -> action
            is String -> action
            is Int -> ctx.getString(action)
            else -> ""
        }

        if (strAction.trim().isNotEmpty()) {
            snackBar.setAction(strAction) {
                listener?.invoke()
            }
        }
    }
    snackBar.show()
}

fun NestedScrollView.scrollToBottom() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY + height)
    smoothScrollBy(0, delta)
}

fun View.setOnSafeClickListener(safeTime: Long = 500, clickListener: (View?) -> Unit) {
    setOnClickListener(SafeOnClickListener.newInstance(safeTime) {
        clickListener(it)
    })
}

fun EditText.hidePasswordText() {
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    setSelection(length())
}

fun EditText.showPasswordText() {
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    setSelection(length())
}