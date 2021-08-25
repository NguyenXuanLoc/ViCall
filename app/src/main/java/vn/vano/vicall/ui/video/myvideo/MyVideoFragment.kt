package vn.vano.vicall.ui.video.myvideo

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_my_video.*
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.FileUtil
import vn.vano.vicall.common.util.NumberUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.video.list.VideoListFragment
import vn.vano.vicall.widget.LayoutVideoChooser
import java.io.File

class MyVideoFragment : BaseFragment<MyVideoView, MyVideoPresenterImp>(), MyVideoView,
    LayoutVideoChooser.VideoChooserListener {

    companion object {
        private const val RC_PERMISSION_READ_EXTERNAL_STORAGE = 1
        private const val RC_PERMISSION_CAMERA = 2
        private const val RC_PICK_VIDEO = 256
        private const val RC_RECORD_VIDEO = 257

        fun newInstance(): MyVideoFragment {
            return MyVideoFragment()
        }
    }

    private var videoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add video list fragment
        addFragment(R.id.frg_container_video, VideoListFragment.newInstance(Constant.PERSONAL))
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
        if ((requestCode == RC_PICK_VIDEO || requestCode == RC_RECORD_VIDEO) && resultCode == RESULT_OK) {
            data?.data?.run {
                uploadVideo(this)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun initView(): MyVideoView {
        return this
    }

    override fun initPresenter(): MyVideoPresenterImp {
        return MyVideoPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_my_video
    }

    override fun initWidgets() {
        // Listeners
        videoChooser.setVideoChooserListener(this)
        btnUpload.setOnSafeClickListener {
            videoChooser.visible()
            videoChooser.enableVisibleAnim()
        }
    }

    override fun fileSizeIsValid(uri: Uri): Boolean {
        return ctx?.videoSizeIsValid(uri) ?: false
    }

    override fun onFileSizeTooLargeError(uri: Uri) {
        ctx?.run {
            toast(
                String.format(
                    getString(R.string.video_size_too_large),
                    Constant.MAXIMUM_VIDEO_SIZE.toString(),
                    NumberUtil.formatNumber(FileUtil.getFileSize(this, uri), 1)
                )
            )
        }
    }

    override fun deleteTempVideoFile() {
        videoFile?.delete()
        videoFile = null
    }

    override fun onClickCamera() {
        recordVideo()
    }

    override fun onClickAlbum() {
        pickVideo()
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

    private fun uploadVideo(fileUri: Uri) {
        ctx?.currentUser?.phoneNumberNonPrefix?.run {
            presenter.uploadVideo(this, fileUri)
        }
    }

    private fun recordVideo() {
        videoFile = FileUtil.recordVideo(self, RC_PERMISSION_CAMERA, RC_RECORD_VIDEO)
    }
}
