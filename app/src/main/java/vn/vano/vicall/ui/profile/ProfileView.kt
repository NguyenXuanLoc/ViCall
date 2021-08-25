package vn.vano.vicall.ui.profile

import android.net.Uri
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseView

interface ProfileView : BaseView {
    fun onVideosMostLoadedSuccess(videos: List<VideoModel>)

    fun onVideosPersonalLoadedSuccess(videos: List<VideoModel>)

    fun updateStatusSuccess(userModel: UserModel)

    fun onVideoUploadedSuccess(videoModel: VideoModel)

    fun fileSizeIsValid(uri: Uri): Boolean

    fun onFileSizeTooLargeError(uri: Uri)

    fun deleteTempVideoFile()

    fun onUserInfoChanged(userModel: UserModel)
}