package vn.vano.vicall.data.interactor

import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import vn.vano.vicall.common.Api
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.data.mapper.convertToModel
import vn.vano.vicall.data.mapper.convertToModels
import vn.vano.vicall.data.model.BaseModel
import vn.vano.vicall.data.model.VideoModel
import java.io.File

private const val MULTIPART_FORM_DATA = "multipart/form-data"

class VideoInteractor : BaseInteractor() {

    fun downloadVideo(url: String): Single<ResponseBody> {
        return service.downloadVideo(url)
    }

    fun getVideoList(
        type: String, category: String? = null, phone: String?, page: Int? = null
    ): Single<List<VideoModel>> {
        val params = HashMap<String, String?>()
        params[Api.TYPE] = type
        category?.run {
            params[Api.CATEGORY] = this
        }
        phone?.run {
            params[Api.MSISDN] = removePhonePrefix()
        }
        page?.run { params[Api.PAGE] = this.toString() }

        params[Api.SIZE] = Constant.PAGE_SIZE.toString()

        return service.getVideoList(params).map {
            val videos = it.data?.convertToModels()

            if (type == Constant.PERSONAL) {
                videos?.map { video ->
                    video.isMyVideo = true
                }
            }

            videos
        }
    }

    fun uploadVideo(phoneNumber: String, videoFile: File): Single<VideoModel> {
        val requestFile = RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), videoFile)
        // MultipartBody.Part is used to send also the actual file name
        val videoBody = requestFile?.run {
            MultipartBody.Part.createFormData(Api.FILE_VIDEO, videoFile.name, this)
        }

        val requestPhone = RequestBody.create(
            MediaType.parse(MULTIPART_FORM_DATA),
            phoneNumber.removePhonePrefix()
        )

        return service.uploadVideo(requestPhone, videoBody).map {
            it.convertToModel()
        }
    }

    fun applyVideo(phoneNumber: String, fileId: String, fromViStore: Boolean): Single<VideoModel> {
        val params = HashMap<String, String>()
        params[Api.MSISDN] = phoneNumber.removePhonePrefix()
        params[Api.FID] = fileId
        params[Api.IN_STORE] = if (fromViStore) {
            "1"
        } else {
            "0"
        }

        return service.applyVideo(params).map {
            it.convertToModel()
        }
    }

    fun deleteVideo(phoneNumber: String, fileId: String): Single<BaseModel> {
        val params = HashMap<String, String>()
        params[Api.MOBILE_NUMBER] = phoneNumber.removePhonePrefix()
        params[Api.FID] = fileId

        return service.deleteVideo(params).map {
            it.convertToModel()
        }
    }
}