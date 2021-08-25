package vn.vano.vicall.data.mapper

import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.data.response.VideoResponse

fun VideoResponse.convertToModel(): VideoModel {
    val response = this
    val data = response.data ?: response
    return VideoModel(data.fileId).apply {
        code = response.code
        message = response.message ?: ""
        downloadCount = data.downloadCount ?: 0
        viewCount = data.viewCount ?: 0
        duration = data.duration?.div(1000) ?: 0
        price = data.price ?: 0
        fileName = data.fileName
        thumbnail = data.thumbnail
        isApplied = data.isUse == 1
        isApproved = data.status == 1
        isWaitingApproved = data.status == 2
    }
}

fun List<VideoResponse>.convertToModels(): ArrayList<VideoModel> {
    return ArrayList(
        map {
            it.convertToModel()
        }
    )
}