package vn.vano.vicall.widget

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.layout_image_chooser.view.*
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.setAnimation
import vn.vano.vicall.common.ext.setOnSafeClickListener

class LayoutVideoChooser(context: Context?, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    private var listener: VideoChooserListener? = null

    init {
        val layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.layout_video_chooser, this, true)

        setOnSafeClickListener {
            // Hide this layout when user clicks transparent area
            hide()
        }

        frlChooserArea.setOnSafeClickListener {
            // Do nothing when clicks on chooser area
            // Don't remove this statement
        }

        imgCamera.setOnSafeClickListener {
            if (ctx.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == true) {
                listener?.onClickCamera()

                // Hide this layout
                hide()
            } else {
                ctx.toast(R.string.err_no_camera)
            }
        }

        imgAlbum.setOnSafeClickListener {
            listener?.onClickAlbum()

            // Hide this layout
            hide()
        }

        imgClose.setOnSafeClickListener {
            // Hide this layout
            hide()
        }
    }

    private fun hide() {
        gone()
        frlChooserArea.gone()
    }

    fun setVideoChooserListener(listener: VideoChooserListener) {
        this.listener = listener
    }

    fun enableVisibleAnim() {
        frlChooserArea.setAnimation(View.VISIBLE, R.anim.slide_in_bottom)
    }

    interface VideoChooserListener {
        fun onClickCamera()

        fun onClickAlbum()
    }
}