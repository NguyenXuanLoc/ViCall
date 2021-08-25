package vn.vano.vicall.data.response

import com.google.gson.annotations.SerializedName

class ActivityResponse(
    @SerializedName("buttonText")
    var buttonText: String? = null,

    @SerializedName("note2")
    var note2: String? = null,

    @SerializedName("note3")
    var note3: String? = null,

    @SerializedName("createdTimeStr")
    var createdTimeStr: String? = null,

    @SerializedName("title")
    var title: String? = null,

    @SerializedName("content")
    var content: String? = null,

    @SerializedName("actionType")
    var actionType: String? = null,

    @SerializedName("hasConfirm")
    var hasConfirm: Int? = null,

    @SerializedName("buttonActionUrl")
    var buttonActionUrl: String? = null,

    @SerializedName("iconType")
    var iconType: String? = null,

    @SerializedName("id")
    var id: Int? = null,

    @SerializedName("buttonAction")
    var buttonAction: String? = null,

    @SerializedName("note1")
    var note1: String? = null,

    @SerializedName("status")
    var status: Int? = null,

    @SerializedName("videoLink")
    var videoLink: String? = null
) : BaseResponse<ActivityResponse>()