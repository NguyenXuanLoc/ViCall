package vn.vano.vicall.ui.video.myvideo

import android.content.Context
import android.net.Uri
import vn.vano.vicall.common.ext.uploadVideo
import vn.vano.vicall.ui.base.BasePresenterImp

class MyVideoPresenterImp(private val ctx: Context) : BasePresenterImp<MyVideoView>(ctx) {

    fun uploadVideo(phoneNumber: String, fileUri: Uri) {
        view?.also { v ->
            if (networkIsAvailable()) {
                if (v.fileSizeIsValid(fileUri)) {
                    // Init work manager
                    ctx.uploadVideo(phoneNumber, fileUri)
                } else {
                    v.onFileSizeTooLargeError(fileUri)
                    v.deleteTempVideoFile()
                }
            } else {
                v.onNetworkError()
            }
        }
    }
}