package vn.vano.vicall.data.mapper

import vn.vano.vicall.data.model.RegisterItemModel
import vn.vano.vicall.data.model.RegisterModel
import vn.vano.vicall.data.response.RegisterItemResponse
import vn.vano.vicall.data.response.RegisterResponse

fun RegisterResponse.convertToModel(): RegisterModel {
    val response = this
    var data = response.data
    return RegisterModel().apply {
        code = response.code
        message = response.message ?: ""
        data?.also {
            isPurchase = data.isPurchase
            arrayPkg = data.arrayPkg?.convertToModels()
        }
    }
}


fun RegisterItemResponse.convertToModel(): RegisterItemModel {
    var response = this
    return RegisterItemModel().apply {
        code = response.code
        name = response.name
        order_num = response.order_num
        info = response.info
    }
}

fun List<RegisterItemResponse>.convertToModels(): ArrayList<RegisterItemModel> {
    return ArrayList(map { it.convertToModel() })
}