package vn.vano.vicall.ui.callerid

import android.Manifest
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackPreparer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import timber.log.Timber
import vn.vano.vicall.R
import vn.vano.vicall.common.CallDirection
import vn.vano.vicall.common.CallState
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.*
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseActivity
import java.io.File

class CallActivity : BaseActivity<CallView, CallPresenterImp>(), CallView, PlaybackPreparer,
    Player.EventListener {

    companion object {
        private const val RC_PERMISSION_CALL_PHONE = 1
        private const val RC_DEFAULT_DIALER_APP = 256
        private const val TIME_TO_DISMISS = 10L * 1000 // 10s

        fun newInstance(ctx: Context, phoneNumber: String, callDirection: CallDirection) {
            Intent(ctx, CallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Key.PHONE_NUMBER, phoneNumber)
                putExtra(Key.CALL_DIRECTION, callDirection)
            }.run {
                ctx.startActivity(this)
            }
        }
    }

    private val parentView by lazy { FrameLayout(ctx) }
    private val callLayout by lazy {
        LayoutInflater.from(ctx).inflate(R.layout.layout_call, null)
    }
    private val cstTop by lazy { callLayout.findViewById(R.id.cst_top) as ConstraintLayout }
    private val imgClose by lazy { callLayout.findViewById(R.id.img_close) as ImageView }
    private val sdvAvt by lazy { callLayout.findViewById(R.id.sdv_avt) as SimpleDraweeView }
    private val lblName by lazy { callLayout.findViewById(R.id.lbl_name) as TextView }
    private val lblStatus by lazy { callLayout.findViewById(R.id.lbl_status) as TextView }
    private val imgBgRightAvt by lazy { callLayout.findViewById(R.id.img_background_right_avt) as ImageView }
    private val lblPhoneNumber by lazy { callLayout.findViewById(R.id.lbl_phone_number) as TextView }
    private val lblCarrierName by lazy { callLayout.findViewById(R.id.lbl_carrier_name) as TextView }
    private val lblAppName by lazy { callLayout.findViewById(R.id.lbl_app_name) as TextView }
    private val playerView by lazy { callLayout.findViewById(R.id.player_view) as PlayerView }
    private val cstPhone by lazy { callLayout.findViewById(R.id.cst_phone) as ConstraintLayout }
    private val vwBarPhone by lazy { callLayout.findViewById(R.id.vw_bar_phone) as View }
    private val imgBgRightPhone by lazy { callLayout.findViewById(R.id.img_background_right_phone) as ImageView }
    private val cstBottomAction by lazy { callLayout.findViewById(R.id.cst_bottom_action) as ConstraintLayout }
    private val imgReCall by lazy { callLayout.findViewById(R.id.img_recall) as ImageView }
    private val imgAddContact by lazy { callLayout.findViewById(R.id.img_add_contact) as ImageView }
    private val labelAddContact by lazy { callLayout.findViewById(R.id.label_add_contact) as TextView }
    private val imgReportSpam by lazy { callLayout.findViewById(R.id.img_spam) as ImageView }
    private val labelSpam by lazy { callLayout.findViewById(R.id.label_spam) as TextView }
    private val imgBlock by lazy { callLayout.findViewById(R.id.img_block) as ImageView }
    private val labelBlock by lazy { callLayout.findViewById(R.id.label_block) as TextView }

    private var phoneNumber: String? = null
    private var callDirection: CallDirection? = null
    private var userOnCall: UserModel? = null
    private val settingsModel by lazy { CommonUtil.getSettings(ctx) }

    private var currentWindow = 0
    private var playbackPosition = 0L
    private var playWhenReady = true
    private var player: SimpleExoPlayer? = null
    private val runnableDismissCallerWindow by lazy {
        Runnable {
            closeCallerIdLayout()
        }
    }
    private val handlerDismissCallerWindow by lazy { Handler() }

    // check to play first video is coming
    private var isPlayVideo = true

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_CALL_PHONE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                call()
            } else {
                // Show call layout again
                showCallWindow()

                // Dismiss caller window
                dismissCallWindowAuto()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_DEFAULT_DIALER_APP) {
            if (resultCode == RESULT_OK) {
                blockNumber()
            } else {
                // Show call layout again
                showCallWindow()

                // Dismiss caller window
                dismissCallWindowAuto()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Get values from extras
        getValuesFromExtras(intent)

        // Must release the previous player(if has) before getting user on call info
        releasePlayer()

        // Get user on call info to display
        getUserOnCallInfo()

        // Hide bottom action
        hideBottomAction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        // Listen call state event to update UI
        presenter.listenCallStateEvent()

        // Listen user on call event to update UI
        presenter.listenUserOnCallEventChange()

        // Get user on call info
        getUserOnCallInfo()
    }

    override fun onStart() {
        super.onStart()
        if (PermissionUtil.isApi24orHigher()) {
            initPlayer()
        }
        EventBusUtil.register(self)
    }

    override fun onResume() {
        super.onResume()
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
        EventBusUtil.unRegister(self)
        /*
        if (PermissionUtil.isApi24orHigher()) {
            releasePlayer()
        }*/
    }

    override fun onDestroy() {
        releasePlayer()
        removeRunnableDismissWindowListener()
        super.onDestroy()
    }

    override fun getExtrasValue() {
        getValuesFromExtras(intent)
    }

    override fun initView(): CallView {
        return this
    }

    override fun initPresenter(): CallPresenterImp {
        return CallPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? = null

    override fun initWidgets() {
    }

    override fun onCallStateChanged(state: CallState) {
        Timber.e("callState: $state")
        if (state == CallState.ACCEPTED) {
            // Need to pause video if it's playing
            // The player does it automatically

            // Hide call window when user accepts the call
            hideCallWindow()
        } else if (state == CallState.IDLE) {
            showCallWindow()

            // Need to dismiss this screen
            dismissCallWindowAuto()

            // Show bottom action
            showBottomAction()
        }
    }

    override fun onGettingLocalUserOnCallSuccess(userModel: UserModel?) {
        userOnCall = userModel
        addCallWindow()

        // Should show caller window or not
        val hasName = userOnCall?.name?.isNotEmpty() == true
        val hasVideo = userOnCall?.urlVideoLocal?.isNotEmpty() == true
        val hasStatus = userOnCall?.statusContent?.isNotBlank() == true
        val isSpam = userOnCall?.isSpam == true

        if (hasInContactList()) {
            if (!hasStatus && !hasVideo) {
                hideCallWindow()
            }
        } else {
            if (!hasName && !isSpam) {
                hideCallWindow()
            }
        }
    }

    override fun onGettingServerUserOnCallSuccess(userModel: UserModel) {
        userOnCall = userModel
        showCallWindow()
        fillCallInfo()
    }

    override fun preparePlayback() {
    }

    private fun addCallWindow() {
        // Remove old views if they're there
        if (parentView.childCount > 0) {
            parentView.removeAllViews()
            windowManager?.removeView(parentView)
        }

        // Remove dismiss callback if it's in process
        removeRunnableDismissWindowListener()

        // Init call layout
        val layoutFlag = if (PermissionUtil.isApi26orHigher()) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSPARENT
        )

        params.gravity = Gravity.CENTER
        callLayout.gone()
        params.width = ActionBar.LayoutParams.MATCH_PARENT
        params.height = ActionBar.LayoutParams.WRAP_CONTENT

        windowManager?.addView(parentView, params)
        parentView.addView(callLayout)
        callLayout.gone()

        val animation = AnimationUtils.loadAnimation(ctx, android.R.anim.slide_in_left)
        animation.duration = 400
        callLayout.visible()
        callLayout.startAnimation(animation)

        setAppNameColor(R.color.orange, 2)

        if (hasInContactList()) {
            imgAddContact.setImageResource(R.drawable.ic_message_black)
            labelAddContact.text = getString(R.string.send_message)
            imgReportSpam.setBackgroundResource(R.drawable.btn_orange_circle)
            imgReportSpam.setImageResource(R.drawable.ic_edit_profile)
            labelSpam.text = getString(R.string.edit)

            imgBlock.gone()
            labelBlock.gone()
        } else {
            imgAddContact.setImageResource(R.drawable.ic_user_plus_grey)
            labelAddContact.text = getString(R.string.save)
            imgReportSpam.setBackgroundResource(R.drawable.btn_circle_yellow)
            imgReportSpam.setImageResource(R.drawable.ic_spam_purple)
            labelSpam.text = getString(R.string.spam)

            if (PermissionUtil.isApi24orHigher()) {
                imgBlock.visible()
                labelBlock.visible()
            } else {
                imgBlock.gone()
                labelBlock.gone()
            }
        }
        if (!networkIsConnected()) {
            fillCallInfo()
        }
        // Listeners
        imgClose.setOnSafeClickListener {
            closeCallerIdLayout()
        }

        imgReCall.setOnSafeClickListener {
            call()
        }

        imgAddContact.setOnSafeClickListener {
            if (hasInContactList()) {
                phoneNumber?.run {
                    closeCallerIdLayout()

                    CommonUtil.composeSms(self, this)
                }
            } else {
                addToContact()
            }
        }

        imgReportSpam.setOnSafeClickListener {
            if (hasInContactList()) {
                phoneNumber?.run {
                    presenter.openContactEditingDefaultApp(this)
                }

                closeCallerIdLayout()
            } else {
                reportSpam()
            }
        }

        imgBlock.setOnSafeClickListener {
            blockNumber()
        }
    }

    private fun showCallWindow() {
        parentView.visible()
    }

    private fun hideCallWindow() {
        parentView.gone()
    }

    private fun closeCallerIdLayout() {
        windowManager?.removeView(parentView)
        finishAndRemoveTask()
    }

    private fun getValuesFromExtras(intent: Intent?) {
        intent?.extras?.run {
            if (containsKey(Key.PHONE_NUMBER)) {
                phoneNumber = getString(Key.PHONE_NUMBER)
            }
            if (containsKey(Key.CALL_DIRECTION)) {
                callDirection = get(Key.CALL_DIRECTION) as CallDirection?
            }
        }
    }

    private fun getUserOnCallInfo() {
        phoneNumber?.also { phone ->
            // Get caller id from local
            presenter.getUserOnCallInfo(phone)

            // Gt caller id info from server
            getCallerId(phone)
        }
    }

    private fun fillCallInfo() {
        // Get user info from shared preference
        phoneNumber?.also { phone ->
            val contact = CommonUtil.getUserContacts(ctx).find {
                it.number.removeSpaces().removePhonePrefix() == phone.removeSpaces()
                    .removePhonePrefix()
            }

            sdvAvt.setImage(
                contact?.avatar ?: userOnCall?.avatar,
                errorImage = R.drawable.ic_mask
            )
            lblName.text = contact?.name ?: userOnCall?.name ?: phone
            lblPhoneNumber.text = phone
            userOnCall?.run { // This user's info has been saved already
                lblCarrierName.text = carrierName
                statusContent?.run {
                    lblStatus.visible()
                    lblStatus.text = this
                } ?: lblStatus.gone()

                if (isSpam) {
                    cstTop.setBackgroundResource(R.drawable.bg_radius_top_red)
                    playerView.gone()
                    lblStatus.visible()
                    lblStatus.text = String.format(
                        getString(R.string._reported),
                        NumberUtil.formatNumber(totalReporter)
                    )
                } else {
                    cstTop.setBackgroundResource(R.drawable.bg_radius_top_primary)
                }

                // Init incoming video if it's incoming call
                if (shouldPlayVideo()) {
                    initPlayer()

                } else {
                    hidePlayerView()
                }
            } ?: run { // Not found user's info
                hidePlayerView()
            }
        }
    }

    private fun initPlayer() {
        if (player == null && shouldPlayVideo()) {
            // Build media source
            var mediaSource = userOnCall?.let { buildMediaSource(it) }
            // Init exo player
            player = SimpleExoPlayer.Builder(ctx).build()
            player?.playWhenReady = playWhenReady
            player?.repeatMode = SimpleExoPlayer.REPEAT_MODE_ONE
            player?.volume = if (settingsModel.videoSoundIsOn) {
                1f
            } else {
                0f
            }
            player?.seekTo(currentWindow, playbackPosition)
            mediaSource?.let { player?.prepare(it, false, false) }
            playerView.player = player
            player?.addListener(this)
        }
    }

    private fun releasePlayer() {
        player?.run {
            this@CallActivity.playWhenReady = playWhenReady
            this@CallActivity.playbackPosition = currentPosition
            this@CallActivity.currentWindow = currentWindowIndex
            release()
            player = null
            Timber.e("Released the player")
        }
    }

    private fun shouldPlayVideo(): Boolean {
        return callDirection == CallDirection.INCOMING
                && settingsModel.callerVideoIsOn
                && userOnCall?.isSpam == false
                && userOnCall?.isActiveService == true
    }

    private fun showPlayerView() {
        playerView.setRatio(self, 16, 9, 0)
        playerView.visible()
        playerView.startAnimation(AnimationUtils.loadAnimation(self, R.anim.slide_in_down))
    }

    private fun hidePlayerView() {
        playerView.gone()
    }

    private fun dismissCallWindowAuto() {
        removeRunnableDismissWindowListener()
        handlerDismissCallerWindow.postDelayed(runnableDismissCallerWindow, TIME_TO_DISMISS)
    }

    private fun removeRunnableDismissWindowListener() {
        handlerDismissCallerWindow.removeCallbacks(runnableDismissCallerWindow)
    }

    private fun showBottomAction() {
        hidePlayerView()
        releasePlayer()
        if (userOnCall?.isSpam == true) {
            cstTop.setBackgroundResource(R.drawable.bg_radius_top_red)
            cstPhone.setBackgroundResource(R.color.red)
        } else {
            cstTop.setBackgroundResource(R.drawable.bg_radius_top_primary)
            cstPhone.setBackgroundResource(R.color.colorPrimary)
        }
        lblPhoneNumber.textColor = R.color.white
        lblCarrierName.textColor = R.color.white50
        imgBgRightAvt.gone()
        vwBarPhone.visible()
        imgBgRightPhone.visible()
        setAppNameColor(R.color.white, lblAppName.length())

        cstBottomAction.visible()
    }

    private fun hideBottomAction() {
        cstPhone.setBackgroundResource(R.drawable.bg_radius_bot_white)
        lblPhoneNumber.textColor = R.color.textColorPrimary
        lblCarrierName.textColor = R.color.textColorSecondary
        imgBgRightAvt.visible()
        vwBarPhone.gone()

        imgBgRightPhone.gone()
        setAppNameColor(R.color.orange, 2)

        cstBottomAction.gone()
    }

    private fun setAppNameColor(colorRes: Int, affectedCharacters: Int) {
        SpannableString(getString(R.string.app_name)).apply {
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(ctx, colorRes)),
                0,
                affectedCharacters,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }.run {
            lblAppName.text = this
        }
    }

    private fun blockNumber() {
        phoneNumber?.run {
            // Remove auto dismiss listener
            removeRunnableDismissWindowListener()

            // Hide layout call
            hideCallWindow()

            // Process block
            if (isDefaultDialerApp()) {
                if (addToBlockList(this)) {
                    toast(String.format(getString(R.string.block_success_), this))

                    // Dismiss caller window
                    dismissCallWindowAuto()
                }
            } else {
                DialogUtil.showAlert(
                    ctx,
                    textMessage = R.string.desc_permission_default_dial,
                    textOk = R.string.ok,
                    textCancel = R.string.cancel,
                    cancelListener = {
                        // Dismiss caller window
                        dismissCallWindowAuto()

                        // Show layout call
                        showCallWindow()
                    },
                    okListener = {
                        requestDefaultDialerApp(self, RC_DEFAULT_DIALER_APP)
                    })
            }
        }
    }

    private fun call() {
        phoneNumber?.run {
            // Remove auto dismiss listener
            removeRunnableDismissWindowListener()

            // Hide layout call
            hideCallWindow()

            if (PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    RC_PERMISSION_CALL_PHONE
                )
            ) {
                // Close caller window
                closeCallerIdLayout()

                // Implement call
                CommonUtil.call(self, this)
            }
        }
    }

    private fun addToContact() {
        phoneNumber?.run {
            val name = if (lblName.text.toString().isNumeric()) {
                null
            } else {
                lblName.text.toString()
            }
            presenter.openContactAddingDefaultApp(this, name)

            closeCallerIdLayout()
        }
    }

    private fun reportSpam() {
        currentUser?.phoneNumberNonPrefix?.also { userNumber ->
            phoneNumber?.also { callerNumber ->
                presenter.reportSpam(userNumber, callerNumber)

                toast(R.string.reported_spam_success)
            }
        }
    }

    private fun hasInContactList(): Boolean {
        return phoneNumber?.let { phone ->
            CommonUtil.getUserContacts(ctx).find {
                it.number.removeSpaces().removePhonePrefix() == phone.removeSpaces()
                    .removePhonePrefix()
            } != null
        } ?: false
    }

    // Check when video ready play to show video
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying && isPlayVideo) {
            showPlayerView()
            isPlayVideo = false
        }
        super.onIsPlayingChanged(isPlaying)
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        playerView.gone()
    }

    private fun buildMediaSource(userModel: UserModel): MediaSource {
        val mediaSource = ConcatenatingMediaSource()
        var source: MediaSource? = null
        if (userModel.urlVideoLocal != null) {
            source = ProgressiveMediaSource.Factory(
                DefaultDataSourceFactory(
                    ctx,
                    CallActivity::class.java.simpleName
                )
            ).createMediaSource(Uri.fromFile(File(userModel.urlVideoLocal)))
        } else {
            if (userModel.videoStream != null) {
                source = if (userModel.videoStream!!.endsWith(Constant.MEDIA_M3U8, true)) {
                    HlsMediaSource.Factory(
                        DefaultDataSourceFactory(
                            ctx,
                            CallActivity::class.java.simpleName
                        )
                    )
                } else {
                    ProgressiveMediaSource.Factory(
                        DefaultDataSourceFactory(
                            ctx,
                            CallActivity::class.java.simpleName
                        )
                    )
                }.createMediaSource(Uri.parse(userModel.videoStream))
            }
        }
        mediaSource.addMediaSource(source)
        return mediaSource
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun showPopup(result: String) {
        if (result == Constant.DISMISS) {
            removeRunnableDismissWindowListener()
            closeCallerIdLayout()
        }
        EventBusUtil.removeAllSticky()
    }
}
