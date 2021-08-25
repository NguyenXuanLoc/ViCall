package vn.vano.vicall.data.model

import com.google.gson.annotations.SerializedName
import vn.vano.vicall.data.response.RegisterItemResponse

class RegisterModel : BaseModel() {
    var isPurchase: Int? = null
    var arrayPkg: List<RegisterItemModel>? = null
}

class RegisterItemModel() {
    var code: String? = null
    var name: String? = null
    var order_num: Int? = null
    var info: String? = null
}