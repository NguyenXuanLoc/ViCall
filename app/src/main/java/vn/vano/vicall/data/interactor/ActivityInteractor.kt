package vn.vano.vicall.data.interactor

import io.reactivex.Single
import vn.vano.vicall.common.Api
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.data.mapper.convertToModel
import vn.vano.vicall.data.mapper.convertToModels
import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.data.model.BaseModel

class ActivityInteractor : BaseInteractor() {

    fun getActivities(
        type: String,
        phoneNumber: String,
        page: String,
        size: String
    ): Single<List<ActivityModel>> {
        val params = HashMap<String, Any>()
        params[Api.TYPE] = type
        params[Api.MSISDN] = phoneNumber.removePhonePrefix()
        params[Api.PAGE] = page
        params[Api.SIZE] = size

        return service.getActivities(params).map {
            it.data?.convertToModels()
        }
    }

    fun readActivity(id: String): Single<BaseModel> {
        val params = HashMap<String, String>()
        params[Api.AID] = id
        return service.readActivity(params).map {
            it.convertToModel()
        }
    }

    fun readNotifyAll(aid: String, phone: String): Single<BaseModel> {
        val param = HashMap<String, String>()
        param[Api.AID] = aid
        param[Api.MOBILE_NUMBER] = phone
        return service.readNotifyAll(param).map { it.convertToModel() }
    }
}