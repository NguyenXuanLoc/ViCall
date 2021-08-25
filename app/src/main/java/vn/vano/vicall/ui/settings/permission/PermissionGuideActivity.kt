package vn.vano.vicall.ui.settings.permission

import kotlinx.android.synthetic.main.activity_permissions_guide.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.openAppSettingsPage
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.ui.base.BaseActivity

class PermissionGuideActivity : BaseActivity<PermissionView, PermissionPresenterImp>(),
    PermissionView {

    override fun initView(): PermissionView {
        return this
    }

    override fun initPresenter(): PermissionPresenterImp {
        return PermissionPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_permissions_guide
    }

    override fun initWidgets() {
        // Init toolbar
        showTitle(R.string.guide_to_allow_other_permissions)
        enableHomeAsUp { finish() }

        // Listener
        btnOpenSettings.setOnSafeClickListener {
            openAppSettingsPage()
        }
    }
}