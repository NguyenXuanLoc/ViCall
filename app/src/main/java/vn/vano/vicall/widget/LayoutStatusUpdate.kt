package vn.vano.vicall.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.layout_status_update.view.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.setAnimation
import vn.vano.vicall.common.ext.setOnSafeClickListener

class LayoutStatusUpdate(context: Context?, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {
    var listener: StatusChooserListener? = null

    init {
        val layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.layout_status_update, this, true)
        setOnSafeClickListener {
            // Hide this layout when user clicks transparent area
            hide()
        }
        frlChooser.setOnSafeClickListener {
            // Do nothing when clicks on chooser area
            // Don't remove this statement
        }
        imgClose.setOnClickListener {
            hide()
        }
        imgUpdate.setOnClickListener {
            hide()
            listener?.onClickUpload(edtStatus.text.toString())
            edtStatus.setText("")
        }
    }

    private fun hide() {
        gone()
        frlChooser.gone()
    }

    fun enableVisibleAnim() {
        frlChooser.setAnimation(View.VISIBLE, R.anim.slide_in_bottom)
    }

    fun setStatusChooseListener(listener: StatusChooserListener) {
        this.listener = listener
    }

    interface StatusChooserListener {
        fun onClickUpload(status: String)
    }
}

