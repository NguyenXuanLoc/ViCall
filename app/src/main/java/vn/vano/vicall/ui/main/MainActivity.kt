package vn.vano.vicall.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.BuildConfig
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.DialogUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.common.util.RxBus
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.data.model.VideoLayoutSwitcherEventModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.dialer.DialerActivity
import vn.vano.vicall.ui.notification.NotificationsActivity
import vn.vano.vicall.ui.profile.ProfileActivity
import vn.vano.vicall.ui.registerservice.RegisterServiceActivity
import vn.vano.vicall.ui.searchingcontact.SearchingContactActivity
import vn.vano.vicall.ui.video.VideoPageFragment

class MainActivity : BaseActivity<MainView, MainPresenterImp>(), MainView {

    companion object {
        private const val RC_PERMISSION_READ_CONTACT = 1

        private const val HOME_INDEX = 0
        private const val CONTACT_INDEX = 1
        private const val CALL_INDEX = 2
        private const val VIDEO_INDEX = 3
        private const val SETTINGS_INDEX = 4
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_CONTACT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkContactPermission()
            } else {
                finish()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translucentStatusBar()

        // Listen user info changed
        presenter.listenUserInfoChanged()

        // Refresh user profile
        getUserProfile()

        // Request contact permission and sync all contact to server if it's the first time open Home page
        if (!CommonUtil.contactsAreSynced(ctx)) {
            showContactPermissionRequestDialog()
        } else {
            syncContactsToServer()
        }
    }

    override fun initView(): MainView {
        return this
    }

    override fun initPresenter(): MainPresenterImp {
        return MainPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_main
    }

    override fun initWidgets() {
        // This screen uses its toolbar, so hide the base toolbar
        hideToolbarBase()

        // Init main pager adapter
        pagerMain.isUserInputEnabled = false
        pagerMain.adapter = MainStateAdapter(self)
        pagerMain.offscreenPageLimit = pagerMain.adapter?.itemCount ?: 1
        navMain.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    pagerMain.currentItem = HOME_INDEX
                    updateHeaderUI()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_contacts -> {
                    pagerMain.currentItem = CONTACT_INDEX
                    updateHeaderUI()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_call -> {
                    openDialerPage()
                    return@setOnNavigationItemSelectedListener false
                }
                R.id.navigation_video -> {
                    pagerMain.currentItem = VIDEO_INDEX
                    updateHeaderUI()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_settings -> {
                    pagerMain.currentItem = SETTINGS_INDEX
                    updateHeaderUI()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }

        // Listeners
        sdvUserAvt.setOnSafeClickListener {
            openActivity(ProfileActivity::class.java)
        }

        lblSearchContact.setOnSafeClickListener {
            SearchingContactActivity.start(self)
        }

        btnMyVideos.setOnSafeClickListener {
            switchVideoPages(btnMyVideos, btnViStore)
        }

        btnViStore.setOnSafeClickListener {
            switchVideoPages(btnViStore, btnMyVideos)
        }

        imgVicallService.setOnSafeClickListener {
            currentUser?.run {
                if (isActiveService) {
                    toast(R.string.registered_service_already)
                } else {
                    openActivity(RegisterServiceActivity::class.java)
                }
            }
        }

        imgNotification.setOnSafeClickListener {
            openActivity(NotificationsActivity::class.java)
        }

        imgSyncContacts.setOnSafeClickListener {
            checkContactPermission()
        }

        imgAddContact.setOnSafeClickListener {
            presenter.openContactAddingDefaultApp()
        }

        imgSwitchVideoLayout.setOnSafeClickListener {
            // Publish video layout changed event
            RxBus.publishVideoLayoutSwitcherEventModel(VideoLayoutSwitcherEventModel())

            // Update selected state to change image resource
            imgSwitchVideoLayout.isSelected = !imgSwitchVideoLayout.isSelected
            if (imgSwitchVideoLayout.isSelected) {
                imgSwitchVideoLayout.setImageResource(R.drawable.ic_listview_black)
            } else {
                imgSwitchVideoLayout.setImageResource(R.drawable.ic_gridview_black)
            }
        }

        imgCallMain.setOnSafeClickListener {
            openDialerPage()
        }
    }

    override fun onUserInfoChanged(userModel: UserModel) {
        fillUserInfo()
    }

    override fun onContactsLoadedSuccess(contacts: ArrayList<ContactModel>) {
        currentUser?.phoneNumberNonPrefix?.run {
            presenter.syncContacts(this, contacts)
        }
    }

    override fun onContactsSyncedSuccess() {
        toast(R.string.sync_success)

        CommonUtil.markContactsAreSynced(ctx)
    }

    private fun fillUserInfo() {
        currentUser?.run {
            sdvUserAvt.setImage(avatar, R.drawable.ic_mask)
            lblUserName.text = name
            if (statusContent?.isNotBlank() == true) {
                lblUserStatus.visible()
                lblUserStatus.text = statusContent
            } else {
                lblUserStatus.gone()
            }

            if (isActiveService) {
                imgVicallService.setImageResource(R.drawable.btn_vicall_service_active)
            } else {
                imgVicallService.setImageResource(R.drawable.btn_vicall_service_inactive)
            }

            if (unreadNotification > 0) {
                imgNotificationBadge.visible()
            } else {
                imgNotificationBadge.gone()
            }
        }
    }

    private fun updateHeaderUI() {
        when (pagerMain.currentItem) {
            HOME_INDEX -> {
                lblSearchContact.visible()
                imgVicallService.visible()
                frlNotification.visible()
                imgSyncContacts.gone()
                imgAddContact.gone()
                cstVideoSwitch.gone()
                imgSwitchVideoLayout.gone()
                lblVersionName.gone()
            }
            CONTACT_INDEX -> {
                lblSearchContact.visible()
                imgVicallService.gone()
                frlNotification.gone()
                imgSyncContacts.visible()
                imgAddContact.visible()
                cstVideoSwitch.gone()
                imgSwitchVideoLayout.gone()
                lblVersionName.gone()
            }
            VIDEO_INDEX -> {
                lblSearchContact.gone()
                imgVicallService.gone()
                frlNotification.gone()
                imgSyncContacts.gone()
                imgAddContact.gone()
                cstVideoSwitch.visible()
                imgSwitchVideoLayout.visible()
                lblVersionName.gone()

                if (!btnViStore.isSelected && !btnMyVideos.isSelected) {
                    switchVideoPages(btnViStore, btnMyVideos)
                }
            }
            SETTINGS_INDEX -> {
                lblSearchContact.gone()
                imgVicallService.gone()
                frlNotification.gone()
                imgSyncContacts.gone()
                imgAddContact.gone()
                cstVideoSwitch.gone()
                imgSwitchVideoLayout.gone()
                lblVersionName.visible()
                lblVersionName.text =
                    String.format(getString(R.string.version_), BuildConfig.VERSION_NAME)
            }
        }
    }

    private fun switchVideoPages(selectedButton: TextView, unselectedButton: TextView) {
        if (!selectedButton.isSelected) {
            selectedButton.isSelected = true
            unselectedButton.isSelected = false

            for (frg in supportFragmentManager.fragments) {
                if (frg is VideoPageFragment) {
                    frg.switchVideoPages()
                    break
                }
            }
        }
    }

    private fun openDialerPage() {
        openActivity(
            DialerActivity::class.java,
            enterAnim = R.anim.slide_in_bottom,
            exitAnim = R.anim.nothing
        )
    }

    private fun checkContactPermission() {
        if (PermissionUtil.isGranted(
                self,
                arrayOf(Manifest.permission.READ_CONTACTS),
                RC_PERMISSION_READ_CONTACT
            )
        ) {
//            presenter.getContactsFromLocal()
            syncContactsToServer()
        }
    }

    private fun showContactPermissionRequestDialog() {
        DialogUtil.showAlert(ctx,
            textMessage = R.string.desc_contact_permission,
            textCancel = R.string.cancel,
            textOk = R.string.ok,
            cancelable = false,
            okListener = {
                checkContactPermission()
            }
        )
    }
}
