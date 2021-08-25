package vn.vano.vicall.data.mapper

import vn.vano.vicall.data.model.OtpModel
import vn.vano.vicall.data.response.OtpResponse

fun OtpResponse.convertToModel(): OtpModel {
    val response = this
    return OtpModel(data).apply {
        code = response.code
        message = response.message ?: ""
    }
}