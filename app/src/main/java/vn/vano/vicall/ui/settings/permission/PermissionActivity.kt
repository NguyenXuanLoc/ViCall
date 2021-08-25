package vn.vano.vicall.ui.settings.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import kotlinx.android.synthetic.main.activity_permissions.*
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.openActivity
import vn.vano.vicall.common.ext.openAppSettingsPage
import vn.vano.vicall.common.ext.requestDrawOverlayPermission
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.callerid.AccessibilityServiceImpl

class PermissionActivity : BaseActivity<PermissionView, PermissionPresenterImp>(), PermissionView {

    companion object {
        private const val RC_PERMISSION_STORAGE = 1
        private const val RC_PERMISSION_CONTACTS = 2
        private const val RC_PERMISSION_PHONE = 3
        private const val RC_PERMISSION_CAMERA = 4
        private const val RC_PERMISSION_CALL_LOG = 5
        private const val RC_DRAW_OVERLAY = 256
        private const val RC_BATTERY_OPTIMIZATION = 257
        private const val RC_ACCESSIBILITY = 258
    }

    private val powerManager by lazy { getSystemService(Context.POWER_SERVICE) as PowerManager }

    override fun onResume() {
        super.onResume()

        // Fill permissions status here to update these status if user comes back from Settings page
        // (Which you don't want to take results from #OnActivityResult())
        fillStoragePermission()
        fillContactsPermission()
        fillPhonePermission()
        fillCameraPermission()
        fillCallLogPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fillStoragePermission(true)
        } else if (requestCode == RC_PERMISSION_CONTACTS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fillContactsPermission(true)
        } else if (requestCode == RC_PERMISSION_PHONE && grantResults.isNotEmpty()) {
            var granted = true
            for (permission in grantResults) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            if (granted) {
                fillContactsPermission(true)
            }
        } else if (requestCode == RC_PERMISSION_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fillCameraPermission(true)
        } else if (requestCode == RC_PERMISSION_CALL_LOG && grantResults.isNotEmpty()) {
            var granted = true
            for (permission in grantResults) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            if (granted) {
                fillCallLogPermission(true)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_DRAW_OVERLAY -> {
                fillDrawOverTopPermission()
            }
            RC_BATTERY_OPTIMIZATION -> {
                fillBatteryOptimizationPermission()
            }
            RC_ACCESSIBILITY -> {
                fillAccessibilityPermission()
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun initView(): PermissionView {
        return this
    }

    override fun initPresenter(): PermissionPresenterImp {
        return PermissionPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_permissions
    }

    override fun initWidgets() {
        // Init toolbar
        showTitle(R.string.grant_permissions)
        enableHomeAsUp { finish() }

        // Listener
        cstDrawOnTop.setOnSafeClickListener {
            requestDrawOverlayPermission(RC_DRAW_OVERLAY)
        }

        cstStorage.setOnSafeClickListener {
            if (swchStoragePermission.isChecked) {
                openAppSettingsPage()
            } else {
                PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    RC_PERMISSION_STORAGE
                )
            }
        }

        cstContact.setOnSafeClickListener {
            if (swchContactPermission.isChecked) {
                openAppSettingsPage()
            } else {
                PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    RC_PERMISSION_CONTACTS
                )
            }
        }

        cstPhone.setOnSafeClickListener {
            if (swchPhonePermission.isChecked) {
                openAppSettingsPage()
            } else {
                PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE),
                    RC_PERMISSION_PHONE
                )
            }
        }

        cstCamera.setOnSafeClickListener {
            if (swchCameraPermission.isChecked) {
                openAppSettingsPage()
            } else {
                PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.CAMERA),
                    RC_PERMISSION_CAMERA
                )
            }
        }

        cstCallLog.setOnSafeClickListener {
            if (swchCallLogPermission.isChecked) {
                openAppSettingsPage()
            } else {
                PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.READ_CALL_LOG/*, Manifest.permission.WRITE_CALL_LOG*/),
                    RC_PERMISSION_CALL_LOG
                )
            }
        }

        cstBatteryOptimization.setOnSafeClickListener {
            if (swchBatteryPermission.isChecked) {
                openBatteryOptimizationPage()
            } else {
                requestIgnoreBatteryOptimization()
            }
        }

        cstAccessibility.setOnSafeClickListener {
            openAccessibilityPage()
        }

        cstShowOnLockScreen.setOnSafeClickListener {
            openGuidePage()
        }

        cstShowPopUp.setOnSafeClickListener {
            openGuidePage()
        }

        // Fill permissions
        fillDrawOverTopPermission()
        fillBatteryOptimizationPermission()
        fillAccessibilityPermission()
    }

    private fun permissionIsGranted(permissions: Array<String>): Boolean {
        return PermissionUtil.isGranted(self, permissions, 0, false)
    }

    private fun fillDrawOverTopPermission() {
        swchDrawOnTopPermission.isChecked = Settings.canDrawOverlays(ctx)
    }

    private fun fillStoragePermission(flag: Boolean = permissionIsGranted(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))) {
        swchStoragePermission.isChecked = flag
    }

    private fun fillContactsPermission(flag: Boolean = permissionIsGranted(arrayOf(Manifest.permission.READ_CONTACTS))) {
        swchContactPermission.isChecked = flag
    }

    private fun fillPhonePermission(
        flag: Boolean = permissionIsGranted(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE
            )
        )
    ) {
        swchPhonePermission.isChecked = flag
    }

    private fun fillCameraPermission(flag: Boolean = permissionIsGranted(arrayOf(Manifest.permission.CAMERA))) {
        swchCameraPermission.isChecked = flag
    }

    private fun fillCallLogPermission(
        flag: Boolean = permissionIsGranted(
            arrayOf(
                Manifest.permission.READ_CALL_LOG
            )
        )
    ) {
        swchCallLogPermission.isChecked = flag
    }

    private fun fillBatteryOptimizationPermission() {
        swchBatteryPermission.isChecked = powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun fillAccessibilityPermission() {
        val prefString = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        swchAccessibilityPermission.isChecked =
            prefString?.contains("$packageName/${AccessibilityServiceImpl::class.java.canonicalName}") ?: false
    }

    private fun requestIgnoreBatteryOptimization() {
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }.run {
            startActivityForResult(this, RC_BATTERY_OPTIMIZATION)
        }
    }

    private fun openBatteryOptimizationPage() {
        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).run {
            startActivityForResult(this, RC_BATTERY_OPTIMIZATION)
        }
    }

    private fun openAccessibilityPage() {
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).run {
            startActivityForResult(this, RC_ACCESSIBILITY)
        }
    }

    private fun openGuidePage() {
        openActivity(PermissionGuideActivity::class.java)
    }
}