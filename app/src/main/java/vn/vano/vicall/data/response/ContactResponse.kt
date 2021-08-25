package vn.vano.vicall.data.response

import com.google.gson.annotations.SerializedName

class ContactResponse(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("mobile")
    val mobile: String? = null,

    @SerializedName("msisdn")
    val msisdn: String? = null,

    @SerializedName("msisdnB")
    val msisdnB: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("birthday")
    val birthday: String? = null,

    @SerializedName("birthdayTime")
    val birthdayTimeStamp: Long? = null,

    @SerializedName("company")
    val company: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("infoStatus")
    val infoStatus: String? = null,

    @SerializedName("infoImage")
    val infoImage: String? = null,

    @SerializedName("activeVicall")
    val activeVicall: Int? = null
) : BaseResponse<ContactResponse>()