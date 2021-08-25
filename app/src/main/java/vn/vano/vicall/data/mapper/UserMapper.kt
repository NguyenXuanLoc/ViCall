package vn.vano.vicall.data.mapper

import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.data.response.UserResponse

fun UserResponse.convertToModel(): UserModel {
    val response = this
    val data = response.data ?: response
    return UserModel(data.msisdn).apply {
        code = response.code
        message = response.message ?: ""
        name = data.name
        nickName = data.nickName
        fullName = data.fullName
        statusContent = data.statusInfo
        avatar = data.avatarInfo
        urlVideo = data.urlVideo
        videoIsEnabled = data.showVideo == 1
        nameIsEnabled = data.showName == 1
        avatarIsEnabled = data.showImage == 1
        statusIsEnabled = data.showStatus == 1
        hasPassword = data.hasPwd == "1"
        isActiveService = data.activeService == "1"
        totalReporter = data.totalSpam ?: 0
        location = data.location
        carrierName = data.carrierName
        isSpam = data.isSpam == 1
        totalReporter = data.totalSpam ?: 0
        location = data.location
        carrierName = data.carrierName
        isSpam = data.isSpam == 1
        birthday = data.birthdayTime
        gender = data.gender
        isViCallUser = data.activeVicall == 1
        loyaltyPoint = data.loyaltyPoint ?: 0
        thumbnailVideo = data.thumbnailVideo
        status = data.status
        loyaltyLevel = data.loyaltyLevel
        unreadNotification = data.unreadNotify ?: 0
        videoStream = data.videoStream
    }
}

fun List<UserResponse>.convertToModels(): ArrayList<UserModel> {
    return ArrayList(
        map {
            it.convertToModel()
        }
    )
}

fun UserModel.updateBy(user: UserModel) {
    code = user.code
    message = user.message
    phoneNumberNonPrefix = user.phoneNumberNonPrefix
    name = user.name
    nickName = user.nickName
    fullName = user.fullName
    statusContent = user.statusContent
    avatar = user.avatar
    urlVideo = user.urlVideo
    videoIsEnabled = user.videoIsEnabled
    nameIsEnabled = user.nameIsEnabled
    avatarIsEnabled = user.avatarIsEnabled
    statusIsEnabled = user.statusIsEnabled
    urlVideoLocal = user.urlVideoLocal
    isLoggedIn = user.isLoggedIn
    hasPassword = user.hasPassword
    isActiveService = user.isActiveService
    totalReporter = user.totalReporter
    location = user.location
    carrierName = user.carrierName
    isSpam = user.isSpam
    birthday = user.birthday
    gender = user.gender
    isViCallUser = user.isViCallUser
    loyaltyPoint = user.loyaltyPoint
    thumbnailVideo = user.thumbnailVideo
    status = user.status
    loyaltyLevel = user.loyaltyLevel
    unreadNotification = user.unreadNotification
    videoStream = user.videoStream
}