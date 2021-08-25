package vn.vano.vicall.data.mapper

import vn.vano.vicall.data.model.PointItemModel
import vn.vano.vicall.data.model.PointModel
import vn.vano.vicall.data.response.PointItemResponse
import vn.vano.vicall.data.response.PointResponse

fun PointResponse.convertToModel(): PointModel {
    val response = this
    val data = response.data
    return PointModel().apply {
        code = response.code
        message = response.message ?: ""
        data?.also { data ->
            totalPoint = data.totalPoint
            lstLoyalty = data.lstLoyalty?.convertToModels()
        }
    }
}

fun PointItemResponse.convertToModel(): PointItemModel {
    val res = this
    return PointItemModel().apply {
        msisdn = res.msisdn
        loyaltyPoint = res.loyaltyPoint.toString()
        loyaltyTimeStr = res.loyaltyTimeStr
        desc = res.desc
    }
}

fun List<PointItemResponse>.convertToModels(): ArrayList<PointItemModel> {
    return ArrayList(
        map {
            it.convertToModel()
        }
    )
}
