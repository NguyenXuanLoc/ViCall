package vn.vano.vicall.data.response

import com.google.gson.annotations.SerializedName

open class BaseResponse<out T>(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null
)