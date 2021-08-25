package vn.vano.vicall.data.response

import com.google.gson.annotations.SerializedName

class UserResponse(
    @SerializedName("msisdn")
    val msisdn: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("nickName")
    val nickName: String? = null,

    @SerializedName("fullName")
    val fullName: String? = null,

    @SerializedName("statusInfo")
    val statusInfo: String? = null,

    @SerializedName("avatarInfo")
    val avatarInfo: String? = null,

    @SerializedName("showVideo")
    val showVideo: Int? = null,

    @SerializedName("showName")
    val showName: Int? = null,

    @SerializedName("showImage")
    val showImage: Int? = null,

    @SerializedName("showStatus")
    val showStatus: Int? = null,

    @SerializedName("videoInfo")
    val urlVideo: String? = null,

    @SerializedName("hasPwd")
    val hasPwd: String? = null,

    @SerializedName("activeService")
    val activeService: String? = null,

    @SerializedName("isSpam")
    val isSpam: Int? = null,

    @SerializedName("birthday")
    val birthday: String? = null,

    @SerializedName("birthdayTime")
    val birthdayTime: Long? = null,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("activeVicall")
    val activeVicall: Int? = null,

    @SerializedName("loyaltyPoint")
    val loyaltyPoint: Int? = null,

    @SerializedName("thumbnailVideo")
    val thumbnailVideo: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("loyaltyLevel")
    val loyaltyLevel: String? = null,

    @SerializedName("totalSpam")
    val totalSpam: Int? = null,

    @SerializedName("carrierName")
    val carrierName: String? = null,

    @SerializedName("unreadNotify")
    val unreadNotify: Int? = null,

    @SerializedName("videoStream")
    val videoStream: String? = null
) : BaseResponse<UserResponse>()