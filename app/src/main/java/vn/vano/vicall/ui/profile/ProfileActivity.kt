package vn.vano.vicall.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Api
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.FileUtil
import vn.vano.vicall.common.util.NumberUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.login.registration.RegistrationActivity
import vn.vano.vicall.ui.registerservice.RegisterServiceActivity
import vn.vano.vicall.ui.video.list.container.VideoListContainerActivity
import vn.vano.vicall.ui.video.player.PlayerActivity
import vn.vano.vicall.widget.LayoutStatusUpdate
import vn.vano.vicall.widget.LayoutVideoChooser
import java.io.File

class ProfileActivity : BaseActivity<ProfileView, ProfilePresenterImp>(), ProfileView,
    LayoutVideoChooser.VideoChooserListener, LayoutStatusUpdate.StatusChooserListener,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private const val RC_PERMISSION_READ_EXTERNAL_STORAGE = 1
        private const val RC_PERMISSION_CAMERA = 2
        private const val RC_PICK_VIDEO = 256
        private const val RC_RECORD_VIDEO = 257
        private const val RC_DELETING_VIDEO = 258
    }

    private var videoFile: File? = null
    private val videoMosts by lazy { ArrayList<VideoModel>() }
    private val videoPersonals by lazy { ArrayList<VideoModel>() }

    private val adapterVideoMostUse by lazy {
        ProfileVideoMostUseAdapter(self, videoMosts) {
            PlayerActivity.start(ctx, videoMosts, videoMosts.indexOf(it))
        }
    }
    private val adapterVideoPersonal by lazy {
        ProfileVideoPersonalAdapter(self, videoPersonals) {
            PlayerActivity.start(
                ctx,
                videoPersonals,
                videoPersonals.indexOf(it),
                RC_DELETING_VIDEO,
                self
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_change -> {
                currentUser?.phoneNumberNonPrefix?.also { phone ->
                    RegistrationActivity.start(
                        self,
                        phoneNumber = phone,
                        action = RegistrationActivity.ACTION_UPDATE
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_READ_EXTERNAL_STORAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickVideo()
        } else if (requestCode == RC_PERMISSION_CAMERA && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordVideo()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((requestCode == RC_PICK_VIDEO || requestCode == RC_RECORD_VIDEO) && resultCode == Activity.RESULT_OK) {
            data?.data?.run {
                uploadVideo(this)
            }
        } else if (requestCode == RC_DELETING_VIDEO && resultCode == Activity.RESULT_OK) {
            data?.extras?.getInt(Key.INDEX)?.run {
                // Remove video which deleted from Player page
                videoPersonals.removeAt(this)
                adapterVideoPersonal.notifyItemRemoved(this)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translucentStatusBar()

        // Get videos
        getVideos()
    }

    override fun initView(): ProfileView {
        return this
    }

    override fun initPresenter(): ProfilePresenterImp {
        return ProfilePresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_profile
    }

    override fun initWidgets() {
        // Init toolbar
        hideToolbarBase()
        applyToolbar(toolbar = toolbar, removeElevation = true)
        enableHomeAsUp(toolbar = toolbar) { finish() }

        // Init swipe refresh layout
        swrProfile.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
        swrProfile.setOnRefreshListener(this)

        // Init recyclerviews
        rclVideoMostUse.layoutManager = GridLayoutManager(ctx, 2, GridLayoutManager.VERTICAL, false)
        rclVideoMostUse.setHasFixedSize(true)
        rclVideoMostUse.isNestedScrollingEnabled = false
        rclVideoMostUse.adapter = adapterVideoMostUse

        rclVideoPersonal.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
        rclVideoPersonal.setHasFixedSize(true)
        rclVideoPersonal.isNestedScrollingEnabled = false
        rclVideoPersonal.adapter = adapterVideoPersonal

        // Fill account info
        fillUserInfo(currentUser)

        // Listeners
        presenter.listenUserInfoChanged()

        statusChoose.setStatusChooseListener(this)

        videoChooser.setVideoChooserListener(this)

        btnUpload.setOnSafeClickListener {
            videoChooser.visible()
            videoChooser.enableVisibleAnim()
        }

        lblStatus.setOnSafeClickListener {
            statusChoose.visible()
            statusChoose.enableVisibleAnim()
        }

        layoutUpdatePremium.setOnSafeClickListener {
            openActivity(RegisterServiceActivity::class.java)
        }

        btnSeeMore.setOnSafeClickListener {
            bundleOf(
                Key.TYPE to Constant.STORE,
                Key.CATEGORY to Constant.MOST_USED
            ).apply {
                openActivity(VideoListContainerActivity::class.java, this)
            }
        }
    }

    override fun dismissRefreshIcon() {
        swrProfile.isRefreshing = false
    }

    override fun onVideosMostLoadedSuccess(videos: List<VideoModel>) {
        videoMosts.clear()
        videoMosts.addAll(videos)
        adapterVideoMostUse.notifyDataSetChanged()

        // Show "Xem thêm" action
        if (videos.size >= Constant.PAGE_SIZE) {
            btnSeeMore.visible()
        }
    }

    override fun onVideosPersonalLoadedSuccess(videos: List<VideoModel>) {
        videoPersonals.clear()
        videoPersonals.addAll(videos)
        adapterVideoPersonal.notifyDataSetChanged()
        if (videoPersonals.isNotEmpty()) {
            lblNotify.invisible()
        }
    }

    override fun updateStatusSuccess(userModel: UserModel) {
        lblStatus.text = userModel.statusContent
        toast(getString(R.string.update_status_success))
    }

    override fun onVideoUploadedSuccess(videoModel: VideoModel) {
        toast(R.string.upload_success)
    }

    override fun fileSizeIsValid(uri: Uri): Boolean {
        return videoSizeIsValid(uri)
    }

    override fun onFileSizeTooLargeError(uri: Uri) {
        toast(
            String.format(
                getString(R.string.video_size_too_large),
                Constant.MAXIMUM_VIDEO_SIZE.toString(),
                NumberUtil.formatNumber(FileUtil.getFileSize(this, uri), 1)
            )
        )
    }

    override fun deleteTempVideoFile() {
        videoFile?.delete()
        videoFile = null
    }

    override fun onUserInfoChanged(userModel: UserModel) {
        // Refresh user info
        fillUserInfo(userModel)
    }

    override fun onClickUpload(status: String) {
        currentUser?.phoneNumberNonPrefix?.also { phone ->
            presenter.updateStatus(Api.STATUS_UPPERCASE, phone, status, ctx)
        }
    }

    override fun onClickCamera() {
        recordVideo()
    }

    override fun onClickAlbum() {
        pickVideo()
    }

    override fun onRefresh() {
        getVideos()
    }

    private fun getVideos() {
        currentUser?.phoneNumberNonPrefix?.also { phone ->
            presenter.getVideoMost(Api.STORE, Constant.MOST_USED, phone, 1, false)
            presenter.getVideoPersonal(Api.PERSONAL, phone, false)
        }
    }

    private fun uploadVideo(fileUri: Uri) {
        currentUser?.phoneNumberNonPrefix?.run {
            presenter.uploadVideo(this, fileUri)
        }
    }

    private fun pickVideo() {
        if (PermissionUtil.isGranted(
                self,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RC_PERMISSION_READ_EXTERNAL_STORAGE
            )
        ) {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
            }.run {
                startActivityForResult(this, RC_PICK_VIDEO)
            }
        }
    }

    private fun recordVideo() {
        videoFile = FileUtil.recordVideo(self, RC_PERMISSION_CAMERA, RC_RECORD_VIDEO)
    }

    private fun fillUserInfo(userModel: UserModel?) {
        userModel?.run {
            lblNameDisplay.text = name
            sdvAvatar.setImage(avatar, R.drawable.ic_mask_with_background)
            lblPhoneNumber.text = "0$phoneNumberNonPrefix"
            lblStatus.text = statusContent ?: getString(R.string.what_are_you_thinking_about)
            lblPoint.text = NumberUtil.formatNumber(loyaltyPoint)
            lblLevel.text = loyaltyLevel

            if (isActiveService) {
                layoutUpdatePremium.gone()
            }
        }
    }
}