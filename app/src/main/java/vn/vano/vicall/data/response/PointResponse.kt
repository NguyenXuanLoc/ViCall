package vn.vano.vicall.data.response

import com.google.gson.annotations.SerializedName

class PointResponse(
    @SerializedName("totalPoint")
    val totalPoint: Int? = null,
    @SerializedName("lstLoyalty")
    val lstLoyalty: List<PointItemResponse>? = null
) : BaseResponse<PointResponse>()

class PointItemResponse(
    @SerializedName("msisdn")
    val msisdn: String,
    @SerializedName("loyaltyPoint")
    val loyaltyPoint: Int,
    @SerializedName("loyaltyTimeStr")
    val loyaltyTimeStr: String,
    @SerializedName("desc")
    val desc: String
)