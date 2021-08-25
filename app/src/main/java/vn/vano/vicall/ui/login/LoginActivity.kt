package vn.vano.vicall.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.OtpModel
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.login.loggedinlist.LoggedInListActivity
import vn.vano.vicall.ui.login.otp.OtpActivity
import vn.vano.vicall.ui.main.MainActivity
import vn.vano.vicall.ui.webview.WebviewActivity

class LoginActivity : BaseActivity<LoginView, LoginPresenterImp>(), LoginView {

    companion object {
        private const val RC_SELECT_OTHER_ACCOUNT = 256
    }

    private var account: UserModel? = null
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translucentStatusBar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_OTHER_ACCOUNT && resultCode == Activity.RESULT_OK) {
            data?.extras?.run {
                if (containsKey(Key.USER_MODEL)) {
                    val acc = getSerializable(Key.USER_MODEL) as UserModel?
                    fillAccountInfo(acc)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun initView(): LoginView {
        return this
    }

    override fun initPresenter(): LoginPresenterImp {
        return LoginPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_login
    }

    override fun initWidgets() {
        // This screen doesn't use toolbar
        hideToolbarBase()

        // Init term & policy
        val term = getString(R.string.term)
        val privacyPolicy = getString(R.string.privacy_policy)
        val termAndPrivacy =
            String.format(getString(R.string.desc_term_and_condition), term, privacyPolicy)
        val termClick = object : ClickableSpan() { // Click on term text
            override fun onClick(widget: View) {
                WebviewActivity.start(ctx, Constant.URL_TERM)
            }
        }
        val policyClick = object : ClickableSpan() { // Click on policy text
            override fun onClick(widget: View) {
                WebviewActivity.start(ctx, Constant.URL_POLICY)
            }
        }
        val termAndPrivacySpannable = SpannableString(termAndPrivacy)
        val termIndex = termAndPrivacy.indexOf(term)
        termAndPrivacySpannable.setSpan(termClick, termIndex, termIndex + term.length, 0)
        termAndPrivacySpannable.setSpan(
            ForegroundColorSpan(Color.WHITE),
            termIndex,
            termIndex + term.length,
            0
        )
        termAndPrivacySpannable.setSpan(UnderlineSpan(), termIndex, termIndex + term.length, 0)
        val policyIndex = termAndPrivacy.indexOf(privacyPolicy)
        termAndPrivacySpannable.setSpan(
            policyClick,
            policyIndex,
            policyIndex + privacyPolicy.length,
            0
        )
        termAndPrivacySpannable.setSpan(
            ForegroundColorSpan(Color.WHITE),
            policyIndex,
            policyIndex + privacyPolicy.length,
            0
        )
        termAndPrivacySpannable.setSpan(
            UnderlineSpan(),
            policyIndex,
            policyIndex + privacyPolicy.length,
            0
        )
        lblTerm.movementMethod = LinkMovementMethod.getInstance()
        lblTerm.setText(termAndPrivacySpannable, TextView.BufferType.SPANNABLE)

        // Listeners
        presenter.registerPhoneTypingListener(txtPhoneNumber)

        imgClear.setOnSafeClickListener {
            showKeyboard(txtPhoneNumber)
        }

        imgExpandAccount.setOnSafeClickListener {
            openActivityForResult(LoggedInListActivity::class.java, RC_SELECT_OTHER_ACCOUNT)
        }

        btnStart.setOnSafeClickListener {
            presenter.getUserByPhone(txtPhoneNumber.text.toString().trim())
        }

        imgTogglePassword.setOnSafeClickListener {
            if (txtPassword.text.isNotEmpty()) {
                if (passwordVisible) {
                    txtPassword.hidePasswordText()
                    imgTogglePassword.setImageResource(R.drawable.ic_eye_black)
                } else {
                    txtPassword.showPasswordText()
                    imgTogglePassword.setImageResource(R.drawable.ic_eye_slash_black)
                }
                passwordVisible = !passwordVisible
            }
        }

        btnNext.setOnSafeClickListener {
            presenter.loginPassword(lblPhoneNumber.text.toString(), txtPassword.text.toString())
        }

        lblForgotPassword.setOnSafeClickListener {
            getOtp()
        }
    }

    override fun onLoginSuccess(userModel: UserModel) {
        // Open Home page
        openActivity(MainActivity::class.java)

        // Close this page
        finish()
    }

    override fun onTypingPhoneNumber(number: String) {
        if (number.isNotBlank()) {
            imgClear.visible()
        } else {
            imgClear.gone()
        }
    }

    // Getting logged in user from shared preference
    override fun onGettingUserByPhoneSuccess(userModel: UserModel?) {
        userModel?.run { // User has logged in before
            fillAccountInfo(this)
        } ?: run { // Has not logged in
            CommonUtil.getDeviceToken(ctx)?.run {
                // Get OTP
                getOtp()
            } ?: run {
                // Get device token
                presenter.getDeviceToken()
            }
        }
    }

    override fun phoneNumberIsValid(): Boolean {
        return txtPhoneNumber.text.isNotBlank()
    }

    override fun onPhoneNumberError() {
        toast(R.string.err_phone_empty)
        txtPhoneNumber.requestFocus()
    }

    override fun passwordIsValid(): Boolean {
        return txtPassword.text.isNotEmpty()
    }

    override fun onPasswordError() {
        toast(R.string.err_password_empty)
        txtPassword.requestFocus()
    }

    override fun onGetOtpSuccess(otpModel: OtpModel, phoneNumber: String) {
        otpModel.otp?.run {
            // Open OTP page
            account?.run {
                OtpActivity.start(ctx, OtpActivity.ACTION_FORGOT_PASSWORD, phoneNumber)
            } ?: run {
                OtpActivity.start(ctx, OtpActivity.ACTION_LOGIN_OTP, phoneNumber)
            }
        } ?: run {
            toast(R.string.err_not_received_otp)
        }
    }

    override fun onGetDeviceTokenSuccess(token: String) {
        getOtp()
    }

    private fun getOtp() {
        presenter.getOtp(txtPhoneNumber.text.toString().trim())
    }

    private fun fillAccountInfo(userModel: UserModel?) {
        account = userModel
        account?.run {
            cstPhoneNumber.gone()
            cstPassword.visible()

            sdvAvt.setImage(avatar, errorImage = R.drawable.ic_mask)
            lblName.text = name
            lblPhoneNumber.text = phoneNumberNonPrefix
        } ?: run {
            cstPhoneNumber.visible()
            cstPassword.gone()

            showKeyboard(txtPhoneNumber)
        }
    }

    // Show keyboard to let user type quickly
    private fun showKeyboard(txt: EditText) {
        txt.setText("")
        txt.requestFocus()
        CommonUtil.showKeyboard(ctx, txt)
    }
}
