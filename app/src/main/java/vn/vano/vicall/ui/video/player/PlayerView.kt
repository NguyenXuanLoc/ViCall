package vn.vano.vicall.ui.video.player

import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseView

interface PlayerView : BaseView {

    fun onVideoAppliedSuccess()

    fun onVideoDeletedSuccess(videoModel: VideoModel)
}