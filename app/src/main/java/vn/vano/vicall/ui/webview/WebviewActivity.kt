package vn.vano.vicall.ui.webview

import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_webview.*
import vn.vano.vicall.R
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.ui.base.BaseActivity

class WebviewActivity : BaseActivity<WebviewView, WebviewPresenterImp>(), WebviewView {

    private var url: String? = null
    private var hasActionButton = false

    companion object {
        fun start(ctx: Context?, url: String, hasActionButton: Boolean = false) {
            Intent(ctx, WebviewActivity::class.java).apply {
                putExtra(Key.URL, url)
                putExtra(Key.HAS_ACTION_BUTTON, hasActionButton)
            }.run {
                ctx?.startActivity(this)
            }
        }
    }

    override fun initView(): WebviewView {
        return this
    }

    override fun getExtrasValue() {
        intent?.extras?.run {
            if (containsKey(Key.URL)) {
                url = getString(Key.URL)
            }
            if (containsKey(Key.HAS_ACTION_BUTTON)) {
                hasActionButton = getBoolean(Key.HAS_ACTION_BUTTON)
            }
        }
    }

    override fun initPresenter(): WebviewPresenterImp {
        return WebviewPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_webview
    }

    override fun initWidgets() {
        // Init toolbar
        enableHomeAsUp { finish() }

        // Init webview
        wvContent.settings.javaScriptEnabled = true
        wvContent.loadUrl(url)
        wvContent.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (view != null) {
                    showTitle(view.title.toString())
                }
            }
        }

        // Show action button
        if (hasActionButton && url != null) {
            url?.also { url ->
                btnAction.visible()
                btnAction.setOnSafeClickListener {
                    CommonUtil.openBrowser(
                        self,
                        url
                    )
                }
            }
        }
    }
}