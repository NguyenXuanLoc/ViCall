package vn.vano.vicall.data.mapper

import vn.vano.vicall.data.model.TokenModel
import vn.vano.vicall.data.response.TokenResponse

fun TokenResponse.convertToModel(): TokenModel {
    val response = this
    return TokenModel(data).apply {
        code = response.code
        message = response.message ?: ""
    }
}