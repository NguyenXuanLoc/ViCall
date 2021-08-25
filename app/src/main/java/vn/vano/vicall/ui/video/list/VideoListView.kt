package vn.vano.vicall.ui.video.list

import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseView

interface VideoListView : BaseView {

    fun onVideosLoadedSuccess(videos: List<VideoModel>)

    fun onLayoutChanged()
}