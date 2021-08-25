package vn.vano.vicall.ui.settings

import kotlinx.android.synthetic.main.fragment_settings.*
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.ext.openActivity
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.DialogUtil
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.login.password.PasswordCreationActivity
import vn.vano.vicall.ui.settings.callinfo.CallInfoSettingsActivity
import vn.vano.vicall.ui.settings.permission.PermissionActivity
import vn.vano.vicall.ui.webview.WebviewActivity

class SettingsFragment : BaseFragment<SettingsView, SettingsPresenterImp>(), SettingsView {
    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun initView(): SettingsView {
        return this
    }

    override fun initPresenter(): SettingsPresenterImp {
        return SettingsPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_settings
    }

    override fun initWidgets() {
        // Listeners
        cstLogout.setOnSafeClickListener {
            ctx?.also { ctx ->
                ctx.currentUser?.run {
                    if (hasPassword) {
                        // Sign user has logged out and save into shared pref
                        CommonUtil.saveLoggedInUserToList(ctx, this, loggingOut = true)

                        // Close app
                        closeApp()
                    } else {
                        DialogUtil.showAlert(ctx,
                            textMessage = R.string.desc_create_password,
                            textCancel = R.string.close,
                            okListener = {
                                phoneNumberNonPrefix?.also { phone ->
                                    PasswordCreationActivity.start(
                                        ctx,
                                        PasswordCreationActivity.ACTION_CREATE_NEW_PASSWORD,
                                        phone
                                    )
                                }
                            },
                            cancelListener = {
                                closeApp()
                            })
                    }
                } ?: run {
                    closeApp()
                }
            }
        }

        cstIntro.setOnSafeClickListener {
            WebviewActivity.start(ctx, Constant.URL_INTRO)
        }

        cstGuide.setOnSafeClickListener {
            WebviewActivity.start(ctx, Constant.URL_GUIDE)
        }

        cstTerm.setOnSafeClickListener {
            WebviewActivity.start(ctx, Constant.URL_TERM)
        }

        cstPolicy.setOnSafeClickListener {
            WebviewActivity.start(ctx, Constant.URL_POLICY)
        }

        cstCallingInfo.setOnSafeClickListener {
            openActivity(CallInfoSettingsActivity::class.java)
        }

        cstGrantPermissions.setOnSafeClickListener {
            openActivity(PermissionActivity::class.java)
        }
    }

    private fun closeApp() {
        parentActivity.finish()
    }
}
