package vn.vano.vicall.data.model

import vn.vano.vicall.common.Constant

data class UserModel(var phoneNumberNonPrefix: String?) : BaseModel() {
    var name: String? = null
    var nickName: String? = null
    var fullName: String? = null
    var statusContent: String? = null
    var avatar: String? = null
    var videoIsEnabled: Boolean = false
    var nameIsEnabled: Boolean = false
    var avatarIsEnabled: Boolean = false
    var statusIsEnabled: Boolean = false
    var urlVideo: String? = null
    var urlVideoLocal: String? = null
    var isLoggedIn: Boolean = false // True: user logged in; False: user logged out
    var hasPassword: Boolean = false
    var isActiveService: Boolean = false
    var isSpam: Boolean = false
    var totalReporter: Int = 0
    var location: String? = null
    var carrierName: String? = null
    var birthday: Long? = null
    var gender: String? = null
    var isViCallUser: Boolean = false
    var loyaltyPoint: Int = 0
    var thumbnailVideo: String? = null
    var status: String? = null
    var loyaltyLevel: String? = null
    var unreadNotification: Int = 0
    var videoStream: String? = null
    fun isMale(): Boolean {
        return gender == Constant.Sex.MALE
    }

    fun isFemale(): Boolean {
        return gender == Constant.Sex.FEMALE
    }
}