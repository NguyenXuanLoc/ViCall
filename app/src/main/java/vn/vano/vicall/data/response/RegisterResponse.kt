package vn.vano.vicall.data.response

import com.google.gson.annotations.SerializedName

class RegisterResponse : BaseResponse<RegisterResponse>() {
    @SerializedName("isPurchase")
    var isPurchase: Int? = null

    @SerializedName("arrayPkg")
    var arrayPkg: List<RegisterItemResponse>? = null
}

class RegisterItemResponse {
    @SerializedName("code")
    var code: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("order_num")
    var order_num: Int? = null

    @SerializedName("info")
    var info: String? = null
}
