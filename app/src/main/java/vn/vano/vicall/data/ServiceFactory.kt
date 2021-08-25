package vn.vano.vicall.data

import com.google.gson.GsonBuilder
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import vn.vano.vicall.BuildConfig
import vn.vano.vicall.common.Api
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.response.*
import java.util.concurrent.TimeUnit

interface ServiceFactory {

    companion object {
        private const val REQUEST_TIMEOUT = 15L
        private const val WRITE_TIMEOUT = 60L
        private const val BASE_URL = "http://api.vicall.vn/v1/"
        private const val HEADER_APP_CODE = "App-Code"
        private const val HEADER_AUTHENTICATION = "Authentication"
        private const val VICALL = "vicall"

        fun create(): ServiceFactory {
            val okHttpClientBuilder = OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader(HEADER_APP_CODE, VICALL)
                        .apply {
                            CommonUtil.getDeviceToken()?.also { token ->
                                addHeader(HEADER_AUTHENTICATION, "Basic:$token")
                            }
                        }
                        .build()

                    chain.proceed(newRequest)
                }

            if (BuildConfig.DEBUG) {
                val httpLoggingInterceptor = HttpLoggingInterceptor()
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)
            }

            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClientBuilder.build())
                .build()

            return retrofit.create(ServiceFactory::class.java)
        }
    }

    @FormUrlEncoded
    @POST(Api.GET_CALLER_ID)
    fun getCallerId(
        @Field(Api.MOBILE_NUMBER_A) phoneNumberNonPrefix: String?,
        @Field(Api.M_IN) callerNumber: String
    ): Single<UserResponse>

    @FormUrlEncoded
    @POST(Api.SEARCH_USER_BY_PHONE)
    fun getUserProfile(
        @Field(Api.MOBILE_NUMBER) phoneNumberNonPrefix: String?
    ): Single<UserResponse>

    @Streaming
    @GET
    fun downloadVideo(@Url url: String): Single<ResponseBody>

    @FormUrlEncoded
    @POST(Api.GET_DEVICE_TOKEN)
    fun getDeviceToken(@FieldMap params: HashMap<String, String>): Single<TokenResponse>

    @FormUrlEncoded
    @POST(Api.GET_OTP)
    fun getOtp(@FieldMap params: HashMap<String, String>): Single<OtpResponse>

    @FormUrlEncoded
    @POST(Api.LOGIN_OTP)
    fun loginOtp(@FieldMap params: HashMap<String, String>): Single<UserResponse>

    @FormUrlEncoded
    @POST(Api.LOGIN_PASSWORD)
    fun loginPassword(@FieldMap params: HashMap<String, String>): Single<UserResponse>

    @Multipart
    @POST(Api.REGISTER_ACCOUNT)
    fun registerAccount(
        @Part(Api.MOBILE_NUMBER) phoneNumber: RequestBody,
        @Part(Api.NAME) name: RequestBody,
        @Part avatarFile: MultipartBody.Part?
    ): Single<UserResponse>

    @POST(Api.GET_ACTIVITIES)
    fun getActivities(@Body params: HashMap<String, Any>): Single<BaseListResponse<ActivityResponse>>

    @POST(Api.UPDATE_USER_SETTINGS)
    fun updateUserSettings(@Body params: HashMap<String, Any>): Single<UserResponse>

    @Multipart
    @POST(Api.UPDATE_USER_PROFILE)
    fun updateUserProfile(
        @Part(Api.MOBILE_NUMBER) phoneNumber: RequestBody,
        @Part(Api.NAME) name: RequestBody?,
        @Part(Api.GENDER) gender: RequestBody?,
        @Part(Api.BIRTHDAY) birthday: RequestBody?,
        @Part avatarFile: MultipartBody.Part?
    ): Single<UserResponse>

    @POST(Api.CONFIG)
    fun getConfig(): Single<RegisterResponse>

    @FormUrlEncoded
    @POST(Api.GET_VICALL_SPAM_LIST)
    fun getViCallSpamList(@FieldMap params: HashMap<String, String>): Single<BaseListResponse<ContactResponse>>

    @FormUrlEncoded
    @POST(Api.MARK_AS_MY_SPAM)
    fun markAsMySpam(@FieldMap params: HashMap<String, String>): Single<BaseResponse<String?>>

    @FormUrlEncoded
    @POST(Api.GET_CONTACTS)
    fun getContacts(@FieldMap params: HashMap<String, String>): Single<BaseListResponse<ContactResponse>>

    @POST(Api.SYNC_CONTACTS)
    fun syncContacts(@Body params: HashMap<String, Any?>): Single<BaseListResponse<ContactResponse>>

    @FormUrlEncoded
    @POST(Api.READ_ACTIVITY)
    fun readActivity(@FieldMap params: HashMap<String, String>): Single<BaseResponse<String?>>

    @FormUrlEncoded
    @POST(Api.GET_POINT_HISTORY)
    fun getPointHistory(@Field(Api.MOBILE_NUMBER) phoneNumber: String): Single<PointResponse>

    @POST(Api.GET_VIDEO_LIST)
    fun getVideoList(@Body params: HashMap<String, String?>): Single<BaseListResponse<VideoResponse>>

    @FormUrlEncoded
    @POST(Api.READ_NOTIFY_ALL)
    fun readNotifyAll(@FieldMap params: HashMap<String, String>): Single<BaseResponse<String?>>


    @Multipart
    @POST(Api.UPLOAD_VIDEO)
    fun uploadVideo(
        @Part(Api.MOBILE_NUMBER) phoneNumber: RequestBody,
        @Part videoFile: MultipartBody.Part?
    ): Single<VideoResponse>

    @POST(Api.APPLY_VIDEO)
    fun applyVideo(@Body params: HashMap<String, String>): Single<VideoResponse>

    @FormUrlEncoded
    @POST(Api.DELETE_VIDEO)
    fun deleteVideo(@FieldMap params: HashMap<String, String>): Single<BaseResponse<String?>>
}