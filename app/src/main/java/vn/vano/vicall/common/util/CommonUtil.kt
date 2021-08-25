package vn.vano.vicall.common.util

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.BuildConfig
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.MyApplication
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.SharedPreferencesUtil.get
import vn.vano.vicall.common.util.SharedPreferencesUtil.set
import vn.vano.vicall.data.mapper.updateBy
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.data.model.SettingsModel
import vn.vano.vicall.data.model.UserModel
import java.lang.reflect.Type
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.*
import kotlin.experimental.and

object CommonUtil {

    private const val SHOWN_INTRO_PAGE = "shown_intro_page"
    private const val USER_MODEL_ = "user_model_"
    private const val LOGGED_IN_LIST = "logged_in_list"
    private const val DEVICE_TOKEN = "device_token"
    private const val SETTINGS_MODEL = "settings_model"
    private const val USER_CONTACTS = "user_contacts"
    private const val CALL_LOG = "call_log"
    private const val CONTACTS_ARE_SYNCED = "CONTACTS_ARE_SYNCED"

    fun markIntroPageShown(ctx: Context) {
        ctx.sharedPref[SHOWN_INTRO_PAGE] = true
    }

    fun introIsShown(ctx: Context): Boolean {
        return ctx.sharedPref[SHOWN_INTRO_PAGE] ?: false
    }

    fun markContactsAreSynced(ctx: Context) {
        ctx.sharedPref[CONTACTS_ARE_SYNCED] = true
    }

    fun contactsAreSynced(ctx: Context): Boolean {
        return ctx.sharedPref[CONTACTS_ARE_SYNCED] ?: false
    }

    fun saveSettings(ctx: Context, model: SettingsModel) {
        ctx.sharedPref["$SETTINGS_MODEL${ctx.currentUser?.phoneNumberNonPrefix}"] = model.toJson()
    }

    fun getSettings(ctx: Context): SettingsModel {
        val str: String? = ctx.sharedPref["$SETTINGS_MODEL${ctx.currentUser?.phoneNumberNonPrefix}"]
        return str?.let {
            Gson().fromJson(it, SettingsModel::class.java)
        } ?: SettingsModel()
    }

    fun saveCallerByPhone(ctx: Context, userModel: UserModel?) {
        ctx.sharedPref["$USER_MODEL_${
            userModel?.phoneNumberNonPrefix?.removeSpaces()
                ?.removePhonePrefix()
        }"] =
            userModel?.toJson()
    }

    fun getCallerByPhone(ctx: Context, phoneNumber: String): UserModel? {
        val strUser: String? =
            ctx.sharedPref["$USER_MODEL_${phoneNumber.removeSpaces().removePhonePrefix()}"]
        return strUser?.let {
            Gson().fromJson(it, UserModel::class.java)
        }
    }

    fun getCurrentUser(ctx: Context): UserModel? {
        val list = getLoggedInList(ctx)
        if (list.isNotEmpty()) {
            return list[0]
        }

        return null
    }

    fun saveLoggedInUserToList(ctx: Context, userModel: UserModel, loggingOut: Boolean = false) {
        removeLoggedInUserFromList(ctx, userModel)
        val list = getLoggedInList(ctx)
        list.add(0, userModel.apply {
            isLoggedIn = !loggingOut
        })
        val json = Gson().toJson(list)
        ctx.sharedPref[LOGGED_IN_LIST] = json

        // Update current user for whole the app
        ctx.currentUser?.updateBy(userModel)

        // Publish user changed event to listeners
        ctx.currentUser?.run {
            RxBus.publishUserModel(this)
        }
    }

    fun getLoggedInUserByPhone(ctx: Context, phoneNumber: String): UserModel? {
        val list = getLoggedInList(ctx)
        for (user in list) {
            if (phoneNumber.removeSpaces()
                    .removePhonePrefix() == user.phoneNumberNonPrefix?.removeSpaces()
            ) {
                return user
            }
        }

        return null
    }

    fun getLoggedInList(ctx: Context): ArrayList<UserModel> {
        val result = arrayListOf<UserModel>()
        val json: String? = ctx.sharedPref[LOGGED_IN_LIST]
        if (json?.isNotBlank() == true) {
            val type: Type = object : TypeToken<ArrayList<UserModel>>() {}.type
            result.addAll(Gson().fromJson<ArrayList<UserModel>>(json, type))
        }

        return result
    }

    fun removeLoggedInUserFromList(ctx: Context, userModel: UserModel) {
        // Get list of logged in user
        val list = getLoggedInList(ctx)

        // Remove the given user if it's in the list
        for (user in list) {
            if (userModel.phoneNumberNonPrefix == user.phoneNumberNonPrefix) {
                list.remove(user)

                // Save the list into shared pref again
                val json = Gson().toJson(list)
                ctx.sharedPref[LOGGED_IN_LIST] = json

                break
            }
        }
    }

    fun saveDeviceToken(ctx: Context, token: String) {
        ctx.sharedPref[DEVICE_TOKEN] = token
    }

    fun getDeviceToken(ctx: Context = MyApplication.instance.ctx): String? {
        return ctx.sharedPref[DEVICE_TOKEN]
    }

    fun showKeyboard(ctx: Context, view: View) {
        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    fun closeKeyboard(activity: AppCompatActivity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    fun closeKeyboardWhileClickOutSide(activity: AppCompatActivity, view: View?) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view?.setOnTouchListener { _, _ ->
                closeKeyboard(activity)
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                closeKeyboardWhileClickOutSide(activity, innerView)
            }
        }
    }

    fun getHeightOfStatusBar(activity: AppCompatActivity): Int {
        val resId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) {
            activity.resources.getDimensionPixelSize(resId)
        } else {
            0
        }
    }

    fun getScreenWidthAsPixel(activity: AppCompatActivity): Int {
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    fun convertDpToPixel(ctx: Context?, dimensionIds: IntArray): Int {
        var result = 0
        ctx?.run {
            for (id in dimensionIds) {
                result += resources.getDimension(id).toInt()
            }
        }

        return result
    }

    fun sendEmail(
        ctx: Context?,
        email: String,
        subject: String,
        content: String,
        bccEmail: String? = null
    ) {
        ctx?.also {
            if (email.isNotEmpty() && email != "null") {
                Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    bccEmail?.run {
                        putExtra(Intent.EXTRA_BCC, arrayOf(bccEmail))
                    }
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, content)
                }.run {
                    try {
                        ctx.startActivity(
                            Intent.createChooser(
                                this,
                                ctx.getString(R.string.feedback)
                            )
                        )
                    } catch (ex: ActivityNotFoundException) {
                        ctx.toast(R.string.no_email_client)
                    }
                }
            } else {
                ctx.toast(R.string.no_email)
            }
        }
    }

    fun shareText(ctx: Context?, subject: String, body: String) {
        ctx?.also {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }.run {
                ctx.startActivity(Intent.createChooser(this, ctx.getString(R.string.invite_friend)))
            }
        }
    }

    /**
     * If you want to call this method on api 23 or higher then you have to check permission in runtime
     * (use PermissionUtil class for reference)
     *
     * @param act
     * @param phoneNumber
     */
    fun call(act: AppCompatActivity, phoneNumber: String?) {
        phoneNumber?.run {
            val number = "tel:$this"
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse(number))
            if (callIntent.resolveActivity(act.packageManager) != null) {
                act.startActivity(callIntent)
            } else {
                act.toast(R.string.err_caller_is_unavailable)
            }
        }
    }

    fun openBrowser(act: AppCompatActivity, url: String, packageName: String? = null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        packageName?.run {
            intent.setPackage(this)
        }
        if (intent.resolveActivity(act.packageManager) != null) {
            act.startActivity(intent)
        } else {
            act.toast(R.string.err_no_browser_installed)
        }
    }

    fun composeSms(ctx: AppCompatActivity, phoneNumber: String, smsContent: String? = null) {
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            smsContent?.also { sms ->
                putExtra("sms_body", sms)
            }
        }.run {
            if (resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(this)
            }
        }
    }

    fun getIPAddress(useIPv4: Boolean = true): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        var isIPv4: Boolean
                        isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) {
                                    sAddr.toUpperCase(Locale.ROOT)
                                } else {
                                    sAddr.substring(0, delim).toUpperCase(Locale.ROOT)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
        }
        return ""
    }

    fun generateSHA256FromString(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.reset()
        val bytes = digest.digest(input.toByteArray())
        val hex = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            hex.append(String.format("%02x", byte and (0xFF.toByte())))
        }

        return hex.toString()
    }

    fun openAppInPlayStore(activity: AppCompatActivity) {
        val uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        } else {
            openBrowser(activity, Constant.URL_VICALL_APP_ON_PLAY_STORE)
        }
    }

    fun setDefaultLanguage(ctx: Context) {
        with(ctx.resources) {
            configuration.setLocale(Locale.getDefault())
            ctx.createConfigurationContext(configuration)
        }
    }

    fun saveUserContacts(ctx: Context?, contacts: List<ContactModel>) {
        ctx?.run {
            val json = Gson().toJson(contacts)
            ctx.sharedPref[USER_CONTACTS] = json
        }
    }

    fun getUserContacts(ctx: Context?): List<ContactModel> {
        val result = arrayListOf<ContactModel>()
        ctx?.run {
            val json: String? = ctx.sharedPref[USER_CONTACTS]
            if (json?.isNotBlank() == true) {
                val type: Type = object : TypeToken<ArrayList<ContactModel>>() {}.type
                result.addAll(Gson().fromJson<ArrayList<ContactModel>>(json, type))
            }
        }

        return result
    }

    fun saveCallLog(ctx: Context?, callLog: List<ContactModel>) {
        ctx?.run {
            val json = Gson().toJson(callLog)
            ctx.sharedPref[CALL_LOG] = json
        }
    }

    fun getCallLog(ctx: Context?): List<ContactModel> {
        val result = arrayListOf<ContactModel>()
        ctx?.run {
            val json: String? = ctx.sharedPref[CALL_LOG]
            if (json?.isNotBlank() == true) {
                val type: Type = object : TypeToken<ArrayList<ContactModel>>() {}.type
                result.addAll(Gson().fromJson<ArrayList<ContactModel>>(json, type))
            }
        }

        return result
    }
}