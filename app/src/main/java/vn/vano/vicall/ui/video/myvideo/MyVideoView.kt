package vn.vano.vicall.ui.video.myvideo

import android.net.Uri
import vn.vano.vicall.ui.base.BaseView

interface MyVideoView : BaseView {

    fun fileSizeIsValid(uri: Uri): Boolean

    fun onFileSizeTooLargeError(uri: Uri)

    fun deleteTempVideoFile()
}