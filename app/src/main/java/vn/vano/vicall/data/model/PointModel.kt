package vn.vano.vicall.data.model

class PointModel() : BaseModel() {
    var totalPoint: Int? = null
    var lstLoyalty: List<PointItemModel>? = null
}

class PointItemModel {
    var msisdn: String? = null
    var loyaltyPoint: String? = null
    var loyaltyTimeStr: String? = null
    var desc: String? = null
}