package vn.vano.vicall.common.ext

import android.net.Uri
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import java.io.File

/**
 * @param src Accept String, Int, Uri
 * @return Unit
 */
fun SimpleDraweeView.setImage(src: Any?, errorImage: Int = R.drawable.img_no_image) {
    hierarchy?.setFailureImage(errorImage)
    when (src) {
        is String -> {
            if (src.isNotEmpty()) {
                setImageURI(src)
            } else {
                setActualImageResource(errorImage)
            }
        }
        is Int -> {
            setActualImageResource(src)
        }
        is Uri -> {
            setImageURI(src, ctx)
        }
        is File -> {
            setImageURI(Uri.fromFile(src), ctx)
        }
        else -> setActualImageResource(errorImage)
    }
}