package vn.vano.vicall.ui.login.otp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_otp.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ErrorCode
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.convertToTimeCounter
import vn.vano.vicall.common.ext.openActivity
import vn.vano.vicall.common.ext.showApiCallError
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.login.password.PasswordCreationActivity
import vn.vano.vicall.ui.login.registration.RegistrationActivity
import vn.vano.vicall.ui.main.MainActivity

class OtpActivity : BaseActivity<OtpView, OtpPresenterImp>(), OtpView {

    private var action: String? = null
    private var phoneNumber: String? = null
    private var strOtp = ""
    private lateinit var otpViews: List<TextView>

    companion object {
        const val ACTION_LOGIN_OTP = "action_login_otp"
        const val ACTION_FORGOT_PASSWORD = "action_forgot_password"

        fun start(ctx: Context?, action: String, phoneNumber: String) {
            ctx?.run {
                Intent(this, OtpActivity::class.java).apply {
                    putExtra(Key.ACTION, action)
                    putExtra(Key.PHONE_NUMBER, phoneNumber)
                }.also { intent ->
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // start counting down
        presenter.setOtpTimeout(Constant.OTP_TIMEOUT)
    }

    override fun getExtrasValue() {
        intent?.extras?.run {
            if (containsKey(Key.ACTION)) {
                action = getString(Key.ACTION)
            }
            if (containsKey(Key.PHONE_NUMBER)) {
                phoneNumber = getString(Key.PHONE_NUMBER)
            }
        }
    }

    override fun initView(): OtpView {
        return this
    }

    override fun initPresenter(): OtpPresenterImp {
        return OtpPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_otp
    }

    override fun initWidgets() {
        // Init toolbar
        showTitle(R.string.validate_otp)
        enableHomeAsUp {
            finish()
        }

        // Init otp
        otpViews = listOf(lbl1st, lbl2nd, lbl3rd, lbl4th, lbl5th, lbl6th)
        txtOtp.requestFocus()
        txtOtp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateOtpUI(s?.toString() ?: "")

                // Call login api when OTP full filled
                if (otpIsValid()) {
                    loginOtp()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.run {
                    val pos = start + before
                    if (pos < length) {
                        strOtp += get(pos)
                    }
                }
            }
        })
        txtOtp.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginOtp()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
    }

    override fun onCountingDown(remainTime: Long) {
        lblCodeExpire.text = if (remainTime > 0) {
            String.format(getString(R.string.code_expire_in_), remainTime.convertToTimeCounter())
        } else {
            getString(R.string.code_expired)
        }
    }

    override fun onOtpVerifiedSuccess(userModel: UserModel) {
        when (action) {
            ACTION_LOGIN_OTP -> {
                // Save logged in user into shared preference
                CommonUtil.saveLoggedInUserToList(ctx, userModel)

                // Open Home page and clear stack
                openActivity(MainActivity::class.java, clearStack = true)
            }
            ACTION_FORGOT_PASSWORD -> {
                // Open create password page
                phoneNumber?.run {
                    PasswordCreationActivity.start(
                        ctx,
                        PasswordCreationActivity.ACTION_FORGOT_PASSWORD,
                        this
                    )
                }

                // Close this page
                finish()
            }
        }
    }

    override fun onUserNotFound() {
        when (action) {
            ACTION_LOGIN_OTP -> {
                // Open Registration page
                RegistrationActivity.start(self, phoneNumber)

                // Close this page
                finish()
            }
            ACTION_FORGOT_PASSWORD -> {
                showApiCallError(ErrorCode.API_ERROR)
            }
        }
    }

    override fun otpIsValid(): Boolean {
        return txtOtp.text.length == Constant.OTP_LENGTH
    }

    override fun onOtpError() {
        toast(String.format(getString(R.string.err_otp_not_full_filled), Constant.OTP_LENGTH))
    }

    private fun updateOtpUI(s: String) {
        strOtp = strOtp.take(s.length)

        for (i in otpViews.indices) {
            if (i < strOtp.length) {
                otpViews[i].text = strOtp[i].toString()
            } else {
                otpViews[i].apply {
                    text = ""
                }
            }
        }
    }

    private fun loginOtp() {
        phoneNumber?.run {
            presenter.loginOtp(this, txtOtp.text.toString())
        }
    }
}
