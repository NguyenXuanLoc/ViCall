package vn.vano.vicall.ui.introduction

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_intro_slider.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.DialogUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.login.LoginActivity

class IntroSliderActivity : BaseActivity<IntroSliderView, IntroSliderPresenterImp>(),
    IntroSliderView {

    companion object {
        private const val RC_PERMISSION_READ_PHONE_STATE = 1
        private const val RC_DRAW_OVERLAY = 256
        private const val RC_ROLE_CALL_SCREENING = 257
        private const val RC_ROLE_CALL_REDIRECTION = 258
    }

    private val introTitles by lazy { resources.getStringArray(R.array.intro_titles) }
    private val introMessages by lazy { resources.getStringArray(R.array.intro_messages) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.getImagesToShow()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_DRAW_OVERLAY) {
            if (Settings.canDrawOverlays(ctx)) {
                requestPermissions()
            } else {
                showNotEnoughPermissionDialog()
            }
        } else if (requestCode == RC_ROLE_CALL_SCREENING) {
            when (resultCode) {
                Activity.RESULT_OK -> requestCallRedirectionRole(RC_ROLE_CALL_REDIRECTION)
                Activity.RESULT_CANCELED -> showNotEnoughPermissionDialog()
            }
        } else if (requestCode == RC_ROLE_CALL_REDIRECTION) {
            when (resultCode) {
                Activity.RESULT_OK -> requestPhoneStatePermission()
                Activity.RESULT_CANCELED -> showNotEnoughPermissionDialog()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_PHONE_STATE) {
            if (grantResults.isNotEmpty()) {
                var granted = true
                for (permission in grantResults) {
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        granted = false
                        break
                    }
                }
                if (granted) {
                    finishIntro()
                } else {
                    showNotEnoughPermissionDialog()
                }
            } else {
                showNotEnoughPermissionDialog()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun initView(): IntroSliderView {
        return this
    }

    override fun initPresenter(): IntroSliderPresenterImp {
        return IntroSliderPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? = R.layout.activity_intro_slider

    override fun initWidgets() {
        // This screen doesn't use toolbar
        hideToolbarBase()

        // Update UI as the first position
        updateUI(0)

        // Listeners
        btnSkip.setOnSafeClickListener {
            showPermissionDescDialog()
        }
        btnPrevious.setOnSafeClickListener {
            pagerIntroduction.currentItem -= 1
        }
        btnNext.setOnSafeClickListener {
            if (isEndOfIntro()) {
                showPermissionDescDialog()
            } else {
                pagerIntroduction.currentItem += 1
            }
        }

        pagerIntroduction.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                updateUI(position)
            }
        })
    }

    override fun slideIsReadyToShow(imageRes: List<Int>) {
        val fragments = ArrayList(imageRes.map {
            IntroItemFragment.newInstance(it)
        })
        pagerIntroduction.adapter = IntroSliderPagerAdapter(supportFragmentManager, fragments)
        indicator.setDotsClickable(false)
        indicator.setViewPager(pagerIntroduction)
    }

    override fun onFinishedIntro() {
        openActivity(LoginActivity::class.java)
        finish()
    }

    private fun updateUI(position: Int) {
        // Update Previous button
        if (position > 0) {
            btnPrevious.visible()
            imgPrevious.visible()
        } else {
            btnPrevious.gone()
            imgPrevious.gone()
        }

        // Update intro title
        if (introTitles.isNotEmpty()) {
            if (position < introTitles.size) {
                lblIntroTitle.text = introTitles[position]
            }
        }

        // Update intro message
        if (introMessages.isNotEmpty()) {
            if (position < introMessages.size) {
                lblIntroMessage.text = introMessages[position]
            }
        }
    }

    private fun isEndOfIntro(): Boolean {
        return pagerIntroduction.currentItem >= pagerIntroduction.adapter?.count!! - 1
    }

    private fun finishIntro() {
        // Mark Intro page in shown
        CommonUtil.markIntroPageShown(ctx)

        // Close Intro page
        presenter.finishIntroduction()
    }

    private fun showPermissionDescDialog() {
        val msg = if (PermissionUtil.isApi29orHigher()) {
            R.string.desc_permission_caller_id_api_29
        } else {
            R.string.desc_permission_caller_id_api_less_than_29
        }

        DialogUtil.showAlert(ctx,
            textMessage = msg,
            textCancel = R.string.no,
            okListener = {
                if (!Settings.canDrawOverlays(ctx)) {
                    requestDrawOverlayPermission(RC_DRAW_OVERLAY)
                } else {
                    requestPermissions()
                }
            },
            cancelListener = {
                showNotEnoughPermissionDialog()
            }
        )
    }

    private fun requestPermissions() {
        if (PermissionUtil.isApi29orHigher()) {
            requestCallScreeningRole(RC_ROLE_CALL_SCREENING)
        } else {
            requestPhoneStatePermission()
        }
    }

    private fun requestPhoneStatePermission() {
        if (PermissionUtil.isGranted(
                ctx,
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CALL_LOG
                ),
                RC_PERMISSION_READ_PHONE_STATE
            )
        ) {
            finishIntro()
        }
    }

    private fun showNotEnoughPermissionDialog() {
        toast("ViCall không có đủ quyền để thực hiện chức năng Caller ID")
    }
}
