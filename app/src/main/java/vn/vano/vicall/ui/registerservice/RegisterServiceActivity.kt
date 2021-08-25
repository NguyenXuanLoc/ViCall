package vn.vano.vicall.ui.registerservice

import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_register_service.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.getUserProfile
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.RegisterItemModel
import vn.vano.vicall.data.model.RegisterModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.webview.WebviewActivity

class RegisterServiceActivity : BaseActivity<RegisterServiceView, RegisterServicePresenterImp>(),
    RegisterServiceView {

    private val registerItemModels by lazy { ArrayList<RegisterItemModel>() }
    private val adapter by lazy {
        RegisterServiceAdapter(ctx as AppCompatActivity, registerItemModels) { sendSms(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translucentStatusBar()

        presenter.getConfig()
    }

    override fun initView(): RegisterServiceView {
        return this
    }

    override fun initPresenter(): RegisterServicePresenterImp {
        return RegisterServicePresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_register_service
    }

    override fun initWidgets() {
        // Hide base toolbar
        hideToolbarBase()

        // Init recyclerview
        rclOption.layoutManager = GridLayoutManager(ctx, 2, GridLayoutManager.VERTICAL, false)
        rclOption.adapter = adapter
        rclOption.setHasFixedSize(true)

        // Init policy & term label
        val term = getString(R.string.term)
        val privacyPolicy = getString(R.string.privacy_policy)
        val termAndPrivacy =
            String.format(getString(R.string.agree_to_terms_and_services), term, privacyPolicy)
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
            ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.buttonPrimary)),
            termIndex,
            termIndex + term.length,
            0
        )
        val policyIndex = termAndPrivacy.indexOf(privacyPolicy)
        termAndPrivacySpannable.setSpan(
            policyClick,
            policyIndex,
            policyIndex + privacyPolicy.length,
            0
        )
        termAndPrivacySpannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(ctx, R.color.buttonPrimary)),
            policyIndex,
            policyIndex + privacyPolicy.length,
            0
        )
        lblTerm.movementMethod = LinkMovementMethod.getInstance()
        lblTerm.setText(termAndPrivacySpannable, TextView.BufferType.SPANNABLE)

        // Listeners
        imgClose.setOnSafeClickListener {
            finish()
        }
    }

    override fun configLoaded(registerModel: RegisterModel) {
        registerItemModels.addAll(registerModel.arrayPkg!!)
        adapter.notifyDataSetChanged()
    }

    private fun sendSms(registerItemModel: RegisterItemModel) {
        if (cbConfirm.isChecked) {
            // Open SMS composer
            CommonUtil.composeSms(
                self,
                Constant.SERVICE_PHONE_NUMBER,
                registerItemModel.code.toString()
            )

            // Call user refreshing api after 15 seconds to update ViCall service status
            getUserProfile(15)
        } else {
            toast(R.string.message_confirm_agree_term_and_policy)
        }
    }
}