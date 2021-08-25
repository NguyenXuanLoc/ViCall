package vn.vano.vicall.data.interactor

import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import vn.vano.vicall.common.Api
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.DateTime
import vn.vano.vicall.common.ext.removePhonePrefix
import vn.vano.vicall.common.ext.removeSpaces
import vn.vano.vicall.common.util.DateTimeUtil
import vn.vano.vicall.data.mapper.convertToModel
import vn.vano.vicall.data.model.*
import java.io.File

private const val MULTIPART_FORM_DATA = "multipart/form-data"

class UserInteractor : BaseInteractor() {

    fun getCallerId(phoneNumber: String?, callerNumber: String): Single<UserModel> {
        return service.getCallerId(
            phoneNumber?.removePhonePrefix(),
            callerNumber.removePhonePrefix()
        ).map {
            it.convertToModel()
        }
    }

    fun getUserProfile(phoneNumber: String): Single<UserModel> {
        return service.getUserProfile(
            phoneNumber.removeSpaces().removePhonePrefix()
        ).map {
            it.convertToModel()
        }
    }

    fun getDeviceToken(firebaseToken: String): Single<TokenModel> {
        val params = HashMap<String, String>()
        params[Api.DEVICE_ID] = firebaseToken
        params[Api.FIREBASE_TOKEN] = firebaseToken
        params[Api.OS] = "1" // 0-unknown; 1-android; 2-ios

        return service.getDeviceToken(params).map {
            it.convertToModel()
        }
    }

    fun getOtp(phoneNumber: String): Single<OtpModel> {
        val params = HashMap<String, String>()
        params[Api.MOBILE_NUMBER] = phoneNumber.removePhonePrefix()

        return service.getOtp(params).map {
            it.convertToModel()
        }
    }

    fun loginOtp(phoneNumber: String, otp: String): Single<UserModel> {
        val params = HashMap<String, String>()
        params[Api.MOBILE_NUMBER] = phoneNumber.removePhonePrefix()
        params[Api.PASSWORD] = otp

        return service.loginOtp(params).map {
            it.convertToModel()
        }
    }

    fun loginPassword(phoneNumber: String, password: String): Single<UserModel> {
        val params = HashMap<String, String>()
        params[Api.MOBILE_NUMBER] = phoneNumber.removePhonePrefix()
        params[Api.PASSWORD] = password.trim()

        return service.loginPassword(params).map {
            it.convertToModel()
        }
    }

    fun registerAccount(phoneNumber: String, name: String, avtFile: File?): Single<UserModel> {
        val requestFile = avtFile?.run {
            RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), this)
        }
        // MultipartBody.Part is used to send also the actual file name
        val avtBody = requestFile?.run {
            MultipartBody.Part.createFormData(Api.FILE_AVATAR, avtFile.name, this)
        }

        val requestPhone = RequestBody.create(
            MediaType.parse(MULTIPART_FORM_DATA),
            phoneNumber.removePhonePrefix()
        )
        val requestName = RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), name.trim())

        return service.registerAccount(requestPhone, requestName, avtBody).map {
            it.convertToModel()
        }
    }

    fun config(): Single<RegisterModel> {
        return service.getConfig().map { it.convertToModel() }
    }

    fun updateUserSettings(
        type: String,
        msisdn: String,
        password: String? = null,
        showName: Boolean? = null,
        showAvatar: Boolean? = null,
        showStatus: Boolean? = null,
        showVideo: Boolean? = null,
        status: String? = null
    ): Single<UserModel> {
        val params = HashMap<String, Any>()
        params[Api.TYPE] = type
        params[Api.MSISDN] = msisdn.removePhonePrefix()
        password?.run {
            params[Api.AUTH_KEY] = this.trim()
        }
        showName?.run {
            params[Api.SHOW_NAME] = if (showName) {
                "1"
            } else {
                "0"
            }
        }
        showAvatar?.run {
            params[Api.SHOW_IMAGE] = if (showAvatar) {
                "1"
            } else {
                "0"
            }
        }
        showStatus?.run {
            params[Api.SHOW_STATUS] = if (showStatus) {
                "1"
            } else {
                "0"
            }
        }
        showVideo?.run {
            params[Api.SHOW_VIDEO] = if (showVideo) {
                "1"
            } else {
                "0"
            }
        }
        status?.run { params[Api.STATUS] = this }
        return service.updateUserSettings(params).map {
            it.convertToModel()
        }
    }

    fun updateUserProfile(
        phoneNumber: String,
        name: String? = null,
        gender: String? = null,
        birthday: Long? = null,
        avtFile: File?
    ): Single<UserModel> {
        val requestPhone = RequestBody.create(
            MediaType.parse(MULTIPART_FORM_DATA),
            phoneNumber.removePhonePrefix()
        )
        val requestName = name?.run {
            RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), trim())
        }
        val requestGender =
            RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), gender ?: Constant.Sex.OTHER)
        val requestBirthday = birthday?.let {
            RequestBody.create(
                MediaType.parse(MULTIPART_FORM_DATA),
                DateTimeUtil.convertTimeStampToDate(birthday, DateTime.Format.DD_MM_YYYY)
            )
        } ?: let {
            RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), "")
        }

        val requestFile = avtFile?.run {
            RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), this)
        }
        // MultipartBody.Part is used to send also the actual file name
        val avtBody = requestFile?.run {
            MultipartBody.Part.createFormData(Api.FILE_AVATAR, avtFile.name, this)
        }

        return service.updateUserProfile(
            requestPhone,
            requestName,
            requestGender,
            requestBirthday,
            avtBody
        ).map {
            it.convertToModel()
        }
    }

    fun getPointHistory(phoneNumber: String): Single<PointModel> {
        return service.getPointHistory(phoneNumber.removePhonePrefix()).map {
            it.convertToModel()
        }
    }

}