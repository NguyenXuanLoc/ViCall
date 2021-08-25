package vn.vano.vicall.ui.video.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.android.synthetic.main.layout_custom_playback_control.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.DialogUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.searchingcontact.SearchingContactActivity
import java.lang.reflect.Type

class PlayerActivity : BaseActivity<PlayerView, PlayerPresenterImp>(), PlayerView,
    PlayerControlView.VisibilityListener {

    companion object {
        fun start(
            ctx: Context,
            videos: List<VideoModel>,
            index: Int,
            requestCode: Int? = null,
            requestContext: Any? = null
        ) {
            val intent = Intent(ctx.ctx, PlayerActivity::class.java).apply {
                putExtra(Key.VIDEO_MODELS, Gson().toJson(videos))
                putExtra(Key.INDEX, index)
            }

            requestCode?.also {
                when (requestContext) {
                    is Fragment -> requestContext.startActivityForResult(intent, requestCode)
                    is AppCompatActivity -> requestContext.startActivityForResult(
                        intent,
                        requestCode
                    )
                }
            } ?: run {
                ctx.startActivity(intent)
            }
        }
    }

    private var startWindowIndex: Int? = null
    private var videoModels: List<VideoModel>? = null

    private var playingVideoModel: VideoModel? = null
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var player: SimpleExoPlayer? = null
    private val playbackStateListener by lazy { PlaybackStateListener(this) }

    override fun onStart() {
        super.onStart()
        if (PermissionUtil.isApi24orHigher()) {
            initPlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (PermissionUtil.isApi23()) {
            initPlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (PermissionUtil.isApi23()) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (PermissionUtil.isApi24orHigher()) {
            releasePlayer()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Get values from extras
        getValuesFromExtras(intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUi()
        }
    }

    override fun getExtrasValue() {
        getValuesFromExtras(intent)
    }

    override fun initView(): PlayerView {
        return this
    }

    override fun initPresenter(): PlayerPresenterImp {
        return PlayerPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_player
    }

    override fun initWidgets() {
        // Init toolbar
        hideToolbarBase()
        applyToolbar(toolbarPlayer)
        enableHomeAsUp(toolbarPlayer) {
            finish()
        }

        // Listeners
        lblApply.setOnSafeClickListener {
            playingVideoModel?.run {
                currentUser?.phoneNumberNonPrefix?.also { phone ->
                    fileId?.also {
                        presenter.applyVideo(phone, fileId, !isMyVideo)
                    }
                }
            }
        }

        lblBuyVideo.setOnSafeClickListener {
            playingVideoModel?.fileId?.also { fileId ->
                CommonUtil.composeSms(self, Constant.CALL_CENTER_1556, "${Constant.BUY} $fileId")
            }
        }

        lblGiveFriend.setOnSafeClickListener {
            playingVideoModel?.fileId?.also { fileId ->
                SearchingContactActivity.start(
                    self,
                    SearchingContactActivity.ACTION_GIVE_FRIEND,
                    fileId
                )
            }
        }

        lblDelete.setOnSafeClickListener {
            deleteVideo()
        }

        imgFullScreen.setOnSafeClickListener {
            requestedOrientation =
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
                    imgFullScreen.setImageResource(R.drawable.ic_exit_full_screen_black)
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    imgFullScreen.setImageResource(R.drawable.ic_full_screen_black)
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
        }
    }

    override fun onVisibilityChange(visibility: Int) {
    }

    override fun onVideoAppliedSuccess() {
        toast(R.string.applied_video_success)
    }

    override fun onVideoDeletedSuccess(videoModel: VideoModel) {
        toast(R.string.deleted_video_success)

        Intent().apply {
            putExtra(Key.INDEX, player?.currentWindowIndex)
        }.run {
            setResult(Activity.RESULT_OK, this)
            finish()
        }
    }

    private fun getValuesFromExtras(intent: Intent?) {
        intent?.extras?.run {
            if (containsKey(Key.INDEX)) {
                startWindowIndex = getInt(Key.INDEX)
            }
            if (containsKey(Key.VIDEO_MODELS)) {
                val json = getString(Key.VIDEO_MODELS)
                json?.run {
                    val type: Type = object : TypeToken<List<VideoModel>>() {}.type
                    videoModels = Gson().fromJson<List<VideoModel>>(json, type)
                }
            }
        }
    }

    private fun buildMediaSource(): MediaSource {
        val mediaSource = ConcatenatingMediaSource()
        videoModels?.run {
            for (video in this) {
                video.fileName?.also { url ->
                    val source = if (url.endsWith(Constant.MEDIA_M3U8, true)) {
                        HlsMediaSource.Factory(
                            DefaultDataSourceFactory(
                                ctx,
                                PlayerActivity::class.java.simpleName
                            )
                        )
                    } else {
                        ProgressiveMediaSource.Factory(
                            DefaultDataSourceFactory(
                                ctx,
                                PlayerActivity::class.java.simpleName
                            )
                        )
                    }.createMediaSource(Uri.parse(url))

                    mediaSource.addMediaSource(source)
                } ?: toast(R.string.url_invalid)
            }
        }

        return mediaSource
    }

    private fun initPlayer() {
        if (player == null) {
            // Build media source
            val mediaSource = buildMediaSource()

            // Init exo player
            player = SimpleExoPlayer.Builder(ctx).build()
            player?.playWhenReady = playWhenReady
            startWindowIndex?.run {
                player?.seekTo(this, playbackPosition)
                startWindowIndex = null
            } ?: player?.seekTo(currentWindow, playbackPosition)
            player?.addListener(playbackStateListener)
            player?.prepare(mediaSource, false, false)
            playerView.player = player
            playerView.setControllerVisibilityListener(this)
        }
    }

    private fun releasePlayer() {
        player?.run {
            this@PlayerActivity.playWhenReady = playWhenReady
            this@PlayerActivity.playbackPosition = currentPosition
            this@PlayerActivity.currentWindow = currentWindowIndex
            removeListener(playbackStateListener)
            release()
            player = null
        }
    }

    private fun hideSystemUi() {
        playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun showCenterProgress() {
        pbPlayer.visible()
    }

    private fun hideCenterProgress() {
        pbPlayer.gone()
    }

    private fun fillActionButtons() {
        playingVideoModel?.run {
            fileId?.run { // fileId != null so that it's remote video
                llAction.visible()
                if (isMyVideo) {
                    lblBuyVideo.gone()
                    lblGiveFriend.gone()
                    lblDelete.visible()
                } else {
                    lblBuyVideo.visible()
                    lblGiveFriend.visible()
                    lblDelete.gone()
                }
            } ?: run { // fileId = null so that it's local video
                llAction.gone()
            }
        }
    }

    private fun getCurrentVideo() {
        playingVideoModel = videoModels?.get(player?.currentWindowIndex ?: 0)
    }

    private fun deleteVideo() {
        playingVideoModel?.also { video ->
            DialogUtil.showAlert(
                ctx,
                textMessage = R.string.confirm_delete_video,
                textOk = R.string.delete,
                textCancel = R.string.no,
                okListener = {
                    currentUser?.phoneNumberNonPrefix?.run {
                        presenter.deleteVideo(this, video)
                    }
                }
            )
        }
    }

    class PlaybackStateListener(private val playerActivity: PlayerActivity) : Player.EventListener {

        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
            super.onTracksChanged(trackGroups, trackSelections)

            // Get current video model
            playerActivity.getCurrentVideo()
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)

            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    playerActivity.hideCenterProgress()
                }
                ExoPlayer.STATE_BUFFERING -> {
                    playerActivity.showCenterProgress()

                    // Get current video model
                    playerActivity.getCurrentVideo()

                    // Show/hide action buttons
                    playerActivity.fillActionButtons()
                }
                ExoPlayer.STATE_READY -> {
                    playerActivity.hideCenterProgress()
                }
                ExoPlayer.STATE_ENDED -> {
                    playerActivity.hideCenterProgress()
                }
            }
        }
    }
}
