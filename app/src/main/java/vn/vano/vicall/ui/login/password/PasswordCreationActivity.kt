package vn.vano.vicall.ui.login.password

import android.content.Context
import android.content.Intent
import kotlinx.android.synthetic.main.activity_create_password.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.closeActivityAndClearStack
import vn.vano.vicall.common.ext.hidePasswordText
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.showPasswordText
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseActivity

class PasswordCreationActivity : BaseActivity<PasswordCreationView, PasswordCreationPresenterImp>(),
    PasswordCreationView {

    private var action: String? = null
    private var phoneNumber: String? = null
    private var passwordVisible = false

    companion object {
        const val ACTION_CREATE_NEW_PASSWORD = "action_create_new_password"
        const val ACTION_FORGOT_PASSWORD = "action_forgot_password"

        fun start(ctx: Context?, action: String, phoneNumber: String) {
            ctx?.run {
                Intent(this, PasswordCreationActivity::class.java).apply {
                    putExtra(Key.ACTION, action)
                    putExtra(Key.PHONE_NUMBER, phoneNumber)
                }.also { intent ->
                    startActivity(intent)
                }
            }
        }
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

    override fun initView(): PasswordCreationView {
        return this
    }

    override fun initPresenter(): PasswordCreationPresenterImp {
        return PasswordCreationPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_create_password
    }

    override fun initWidgets() {
        txtPassword.requestFocus()

        // Init toolbar
        showTitle(R.string.create_password)
        enableHomeAsUp {
            finish()
        }

        // Listeners
        imgTogglePassword.setOnSafeClickListener {
            togglePassword()
        }

        imgToggleRetypePassword.setOnSafeClickListener {
            togglePassword()
        }

        btnConfirm.setOnSafeClickListener {
            phoneNumber?.also { phone ->
                when (action) {
                    ACTION_CREATE_NEW_PASSWORD -> Constant.PASSWD
                    ACTION_FORGOT_PASSWORD -> Constant.FORGOT_PASSWD
                    else -> null
                }?.also { type ->
                    presenter.createPassword(type, phone, txtPassword.text.toString())
                }
            }
        }
    }

    override fun onPasswordCreatedSuccess(userModel: UserModel) {
        toast(R.string.created_password_success)
        when (action) {
            ACTION_CREATE_NEW_PASSWORD -> closeActivityAndClearStack()
            ACTION_FORGOT_PASSWORD -> finish()
        }
    }

    override fun passwordIsValid(): Boolean {
        return txtPassword.text.isNotBlank()
    }

    override fun onPasswordError() {
        toast(R.string.err_password_empty)
        txtPassword.requestFocus()
    }

    override fun passwordRetypeIsValid(): Boolean {
        return txtRetypePassword.text.toString().trim() == txtPassword.text.toString().trim()
    }

    override fun onPasswordRetypeError() {
        toast(R.string.err_password_retype_not_match)
        txtRetypePassword.requestFocus()
    }

    private fun togglePassword() {
        if (txtPassword.text.isNotEmpty() || txtRetypePassword.text.isNotEmpty()) {
            if (passwordVisible) {
                txtPassword.hidePasswordText()
                txtRetypePassword.hidePasswordText()
                imgTogglePassword.setImageResource(R.drawable.ic_eye_black)
                imgToggleRetypePassword.setImageResource(R.drawable.ic_eye_black)
            } else {
                txtPassword.showPasswordText()
                txtRetypePassword.showPasswordText()
                imgTogglePassword.setImageResource(R.drawable.ic_eye_slash_black)
                imgToggleRetypePassword.setImageResource(R.drawable.ic_eye_slash_black)
            }
            passwordVisible = !passwordVisible
        }
    }
}
