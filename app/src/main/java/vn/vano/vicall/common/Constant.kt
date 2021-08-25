package vn.vano.vicall.common

import vn.vano.vicall.BuildConfig

object Constant {
    const val URL_TERM = "http://vicall.vn/dieu-khoan"
    const val URL_POLICY = "http://vicall.vn/chinh-sach"
    const val URL_GUIDE = "http://vicall.vn/huong-dan"
    const val URL_INTRO = "http://vicall.vn/gioi-thieu"
    const val URL_VICALL_APP_ON_PLAY_STORE =
        "http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
    const val MEDIA_M3U8 = ".m3u8"
    const val OTP_TIMEOUT = 300L // Otp will be expired in 5 minutes
    const val OTP_LENGTH = 6 // Otp length is 6 characters
    const val PAGE_SIZE = 20
    const val MAXIMUM_VIDEO_SIZE = 50 // ~50MB
    const val NOTIFY = "NOTIFY"
    const val PASSWD = "PASSWD"
    const val FORGOT_PASSWD = "FORGOT_PASSWD"
    const val SETTING = "SETTING"
    const val MARK = "MARK"
    const val UNMARK = "UNMARK"
    const val SPAM = "SPAM"
    const val STORE = "STORE"
    const val PERSONAL = "PERSONAL"
    const val FREE = "FREE"
    const val LASTEST = "LASTEST"
    const val MOST_USED = "MOST_USED"
    const val DK = "DK"
    const val SMS_REG = "sms_reg"
    const val SMS_VIDEO = "sms_video"
    const val VIDEO_STORE = "video_store"
    const val VC = "VC"
    const val SERVICE_PHONE_NUMBER = "1556"
    const val HOME = "home"
    const val GIVE = "TANG"
    const val BUY = "MUA"
    const val CALL_CENTER_1556 = "1556"
    const val VICALL = "VICALL"
    const val CANCEL = "CANCEL"
    const val EVENT = "EVENT"
    const val SMS = "SMS"
    const val NOTIFY_SELF_APP = "NOTIFY_SELF_APP"
    const val NOTIFY_HTML = "NOTIFY_HTML"
    const val REG_SERVICE = "REG_SERVICE"
    const val UNREG_SERVICE = "UNREG_SERVICE"
    const val HTTP = "http"
    const val DISMISS = "dismiss"

    object Sex {
        const val OTHER = "0"
        const val MALE = "1"
        const val FEMALE = "2"
    }
}

object ResponseCode {
    const val ERROR = "0"
    const val SUCCESS = "1"
    const val AUTH_ERROR = "2"
    const val OTP_INVALID = "3"
    const val NOT_FOUND = "4"
    const val WARNING = "5"
    const val TOKEN_EXPIRED = "6"
    const val TOKEN_INVALID = "7"
    const val EXCEPTION = "9999"
}

object ErrorCode {
    const val API_ERROR = 1 // Api call error (response code is different 200)
    const val FIREBASE_INSTANCE_TOKEN_NULL = 2
    const val DEVICE_TOKEN_NULL = 3
    const val GET_CALL_LOG = 4
}

object DateTime {

    object Format {
        const val DD_MM_YYYY = "dd/MM/yyyy"
        const val HH_MM_DD_MM_YYYY = "HH:mm, dd/MM/yyyy"
        const val HH_MM = "HH:mm"
    }

    object Unit {
        const val SECOND = "second"
        const val MILLISECOND = "millisecond"
    }
}

object Api {
    // Endpoint
    private const val AUTH = "auth/"
    private const val VICALL = "vicall/"
    const val SEARCH_USER_BY_PHONE = "${VICALL}search_customer"
    const val GET_CALLER_ID = "${VICALL}call_in"
    const val GET_DEVICE_TOKEN = "${AUTH}get_token"
    const val GET_OTP = "${AUTH}get_otp"
    const val LOGIN_OTP = "${AUTH}login_otp"
    const val LOGIN_PASSWORD = "${AUTH}login"
    const val REGISTER_ACCOUNT = "${VICALL}register"
    const val UPDATE_USER_SETTINGS = "${VICALL}update_customer"
    const val UPDATE_USER_PROFILE = "${VICALL}update_customer_info"
    const val GET_ACTIVITIES = "${VICALL}get_activity"
    const val READ_ACTIVITY = "${VICALL}read_activity"
    const val CONFIG = "${VICALL}config"
    const val MARK_AS_MY_SPAM = "${VICALL}mark_vspam"
    const val GET_VICALL_SPAM_LIST = "${VICALL}get_vspam"
    const val GET_CONTACTS = "${VICALL}get_contact"
    const val SYNC_CONTACTS = "${VICALL}sync_contact"
    const val GET_POINT_HISTORY = "${VICALL}get_loyalty"
    const val GET_VIDEO_LIST = "${VICALL}get_video"
    const val READ_NOTIFY_ALL = "${VICALL}read_activity"
    const val UPLOAD_VIDEO = "${VICALL}upload_video"
    const val APPLY_VIDEO = "${VICALL}apply_video"
    const val DELETE_VIDEO = "${VICALL}delete_video"

    // Parameter
    const val MSISDN = "msisdn"
    const val DEVICE_ID = "d"
    const val OS = "o"
    const val FIREBASE_TOKEN = "f"
    const val MOBILE_NUMBER = "m"
    const val MOBILE_NUMBER_A = "ma"
    const val MOBILE_NUMBER_B = "mb"
    const val M_IN = "m_in"
    const val PASSWORD = "p"
    const val NAME = "n"
    const val FILE_AVATAR = "fa"
    const val FILE_VIDEO = "fv"
    const val TYPE = "type"
    const val PAGE = "page"
    const val SIZE = "size"
    const val T = "t"
    const val AUTH_KEY = "auth_key"
    const val ACTION = "act"
    const val AID = "aid"
    const val ARR_CONTACT = "arrContact"
    const val CATEGORY = "category"
    const val SHOW_VIDEO = "showVideo"
    const val SHOW_NAME = "showName"
    const val SHOW_IMAGE = "showImage"
    const val SHOW_STATUS = "showStatus"
    const val STATUS = "status"
    const val STORE = "STORE"
    const val PERSONAL = "PERSONAL"
    const val STATUS_UPPERCASE = "STATUS"
    const val FID = "fid"
    const val IN_STORE = "in_store"
    const val GENDER = "g"
    const val BIRTHDAY = "b"
}

object Key {
    const val USER_MODEL = "user_model"
    const val PHONE_NUMBER = "phone_number"
    const val CALL_DIRECTION = "call_direction"
    const val URL = "url"
    const val FILE_PATH = "file_path"
    const val FILE_URI_PATH = "file_uri_path"
    const val FIREBASE_TOKEN = "firebase_token"
    const val ACTIVITY_MODEL = "activity_model"
    const val VIDEO_MODELS = "video_models"
    const val ALL = "ALL"
    const val ACTION = "action"
    const val ID = "id"
    const val INDEX = "index"
    const val CONTACT_MODEL = "contact_model"
    const val HAS_ACTION_BUTTON = "has_action_button"
    const val TYPE = "type"
    const val CATEGORY = "category"
    const val COUNT_NUMBER = "count_number"
}

enum class CallDirection {
    INCOMING, OUTGOING
}

enum class CallState {
    IDLE, RINGING, ACCEPTED, CALLING
}