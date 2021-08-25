package vn.vano.vicall.data.mapper

import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.data.response.ActivityResponse

fun ActivityResponse.convertToModel(): ActivityModel {
    val response = this
    val data = response.data ?: response
    return ActivityModel().apply {
        code = response.code
        message = response.message ?: ""
        buttonText = data.buttonText
        note2 = data.note2
        note3 = data.note3
        createdTimeStr = data.createdTimeStr
        title = data.title
        content = data.content
        actionType = data.actionType
        buttonActionUrl = data.buttonActionUrl
        iconType = data.iconType
        id = data.id
        buttonAction = data.buttonAction
        note1 = data.note1
        videoLink = data.videoLink
        isRead = data.status == 2
        hasActionButton = data.hasConfirm == 1
    }
}

fun List<ActivityResponse>.convertToModels(): ArrayList<ActivityModel> {
    return ArrayList(map {
        it.convertToModel()
    })
}