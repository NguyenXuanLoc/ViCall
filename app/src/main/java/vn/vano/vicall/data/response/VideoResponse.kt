package vn.vano.vicall.data.response

import com.google.gson.annotations.SerializedName

class VideoResponse(
    @SerializedName("fileId")
    val fileId: String? = null,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("downloadCount")
    val downloadCount: Int? = null,

    @SerializedName("viewCount")
    val viewCount: Int? = null,

    @SerializedName("fileName")
    val fileName: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: String? = null,

    @SerializedName("price")
    val price: Int? = null,

    @SerializedName("duration")
    val duration: Long? = null,

    @SerializedName("isUse")
    val isUse: Int? = null
) : BaseResponse<VideoResponse>()