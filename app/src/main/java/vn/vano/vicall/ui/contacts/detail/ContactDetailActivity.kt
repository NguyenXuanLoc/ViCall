package vn.vano.vicall.ui.contacts.detail

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_contact_detail.*
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.DateTime
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.DateTimeUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.video.player.PlayerActivity
import java.util.*

class ContactDetailActivity : BaseActivity<ContactDetailView, ContactDetailPresenterImp>(),
    ContactDetailView {

    companion object {
        private const val RC_PERMISSION_CALL_PHONE = 2
        private const val RC_EDIT_CONTACT = 256
        private const val RC_DEFAULT_DIALER_APP = 257

        fun start(ctx: Context, contactModel: ContactModel?) {
            if (contactModel != null) {
                Intent(ctx, ContactDetailActivity::class.java).apply {
                    putExtra(Key.CONTACT_MODEL, contactModel)
                }.run {
                    ctx.startActivity(this)
                }
            }
        }
    }

    private var contactModel: ContactModel? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == RC_PERMISSION_CALL_PHONE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            call()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_EDIT_CONTACT && resultCode == Activity.RESULT_OK) {
            data?.data?.run {
                val updatedLookUpKey = this.toString().split("/").run {
                    if (size >= 2) {
                        get(size - 2)
                    } else {
                        null
                    }
                }
                presenter.getContactDetailFromLookUpKey(updatedLookUpKey) {
                    mergeContactInfo(contact = it)
                    fillContactInfo()
                }
            }
        } else if (requestCode == RC_DEFAULT_DIALER_APP && resultCode == RESULT_OK) {
            blockNumber()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Only show option menu if lookUpUri found
        menuInflater.inflate(R.menu.detail_menu, menu)
        contactModel?.lookUpUri?.run {
            menu?.findItem(R.id.menu_change)?.title = getString(R.string.edit)
        } ?: run {
            menu?.findItem(R.id.menu_change)?.title = getString(R.string.add)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_change -> {
                contactModel?.lookUpUri?.also { lookUpUri ->
                    presenter.openContactEditingDefaultApp(lookUpUri, RC_EDIT_CONTACT)
                } ?: run {
                    contactModel?.number?.also { number ->
                        presenter.openContactAddingDefaultApp(number)
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getExtrasValue() {
        intent?.extras?.run {
            if (containsKey(Key.CONTACT_MODEL)) {
                contactModel = getSerializable(Key.CONTACT_MODEL) as ContactModel?

                // Merge contact model with local models
                contactModel?.also { contact ->
                    var callerId: UserModel? = null

                    CommonUtil.getCallerByPhone(ctx, contact.number)?.also { caller ->
                        callerId = caller
                    }

                    mergeContactInfo(callerId = callerId)
                }
            }
        }
    }

    override fun initView(): ContactDetailView {
        return this
    }

    override fun initPresenter(): ContactDetailPresenterImp {
        return ContactDetailPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_contact_detail
    }

    override fun initWidgets() {
        // Init toolbar
        applyToolbar(background = R.color.white, removeElevation = true)
        enableHomeAsUp(backArrowColorResId = R.color.buttonPrimary) { finish() }

        if (PermissionUtil.isApi24orHigher()) {
            imgBlock.visible()
            lblBlock.visible()

            contactModel?.number?.run {
                if (isBlocked(this)) {
                    lblBlock.text = getString(R.string.unblock)
                } else {
                    lblBlock.text = getString(R.string.block)
                }
            }
        }

        // Fill contact's info
        fillContactInfo()

        // Get call detail if call id is null
        if (contactModel?.callId == null) {
            contactModel?.number?.run {
                presenter.getCallDetailByPhoneNumber(this)
            }
        }

        // Listeners
        imgCall.setOnSafeClickListener {
            call()
        }

        imgMessage.setOnSafeClickListener {
            contactModel?.run {
                CommonUtil.composeSms(self, number)
            }
        }

        imgSpam.setOnSafeClickListener {
            currentUser?.phoneNumberNonPrefix?.also { userNumber ->
                contactModel?.run {
                    presenter.reportSpam(userNumber, number)
                }
            }
        }

        imgBlock.setOnSafeClickListener {
            if (isDefaultDialerApp()) {
                blockNumber()
            } else {
                requestDefaultDialerApp(self, RC_DEFAULT_DIALER_APP)
            }
        }

        sdvVideoThumb.setOnSafeClickListener {
            contactModel?.urlVideoLocal?.also { videoPath ->
                PlayerActivity.start(
                    ctx,
                    listOf(
                        VideoModel(null).apply {
                            fileName = videoPath
                        }
                    ),
                    0
                )
            }
        }

        lblInviteUseViCall.setOnSafeClickListener {
            contactModel?.run {
                CommonUtil.composeSms(self, number, Constant.URL_VICALL_APP_ON_PLAY_STORE)
            }
        }
    }

    override fun onCallDetailLoaded(call: ContactModel) {
        mergeContactInfo(call = call)
        fillContactInfo()
    }

    override fun onReportedSpamSuccess() {
        toast(R.string.reported_spam_success)
    }

    override fun onUnblockedSuccess(contact: ContactModel) {
        toast(String.format(getString(R.string.unblock_success_), contact.number))
        lblBlock.text = getString(R.string.block)
    }

    private fun mergeContactInfo(
        callerId: UserModel? = null,
        call: ContactModel? = null,
        contact: ContactModel? = null
    ) {
        // Merge caller id info
        contactModel?.avatar = contact?.avatar ?: contactModel?.avatar ?: callerId?.avatar
        contactModel?.name = contact?.name ?: contactModel?.name ?: callerId?.name
        contactModel?.number = contact?.number ?: contactModel?.number ?: ""
        contactModel?.lookUpUri = contact?.lookUpUri ?: contactModel?.lookUpUri
        contactModel?.status = contactModel?.status ?: callerId?.status
        contactModel?.thumbnailVideo = callerId?.thumbnailVideo ?: contactModel?.thumbnailVideo
        contactModel?.urlVideoLocal = callerId?.urlVideoLocal ?: contactModel?.urlVideoLocal

        // Merge call log info
        call?.run {
            contactModel?.callId = callId
            contactModel?.date = date
            contactModel?.duration = duration
            contactModel?.callType = callType
        }
    }

    private fun fillContactInfo() {
        contactModel?.run {
            sdvAvatar.setImage(avatar, errorImage = R.drawable.ic_mask_with_background)
            if (isViCallUser) {
                imgViCallBadge.visible()
            } else {
                imgViCallBadge.gone()
            }
            lblName.text = name ?: getString(R.string.unknown)
            status?.run {
                lblStatus.visible()
                lblStatus.text = this
            } ?: lblStatus.gone()
            lblPhoneNumber.text = number

            callId?.run {
                cstCallInfo.visible()
                date?.also { date ->
                    lblTime.text = String.format(
                        getString(R.string.call_time_),
                        DateTimeUtil.convertTimeStampToDate(date, DateTime.Format.HH_MM),
                        DateTimeUtil.getDateDiff(ctx, date, Calendar.getInstance().timeInMillis)
                    )
                }

                callType?.run {
                    when {
                        isIncomingCall() -> {
                            lblCallType.text = getString(R.string.incoming_call)
                        }
                        isMissedCall() -> {
                            lblCallType.text = getString(R.string.missed_call)
                        }
                        isOutgoingCall() -> {
                            lblCallType.text = getString(R.string.outgoing_call)
                        }
                        isRejectedCall() -> {
                            lblCallType.text = getString(R.string.rejected)
                        }
                        isBlockedCall() -> {
                            lblCallType.text = getString(R.string.blocked)
                        }
                        else -> lblCallType.gone()
                    }
                }

                duration?.run {
                    lblDuration.text = DateTimeUtil.getDuration(ctx, this)
                }
            } ?: cstCallInfo.gone()

            urlVideoLocal?.run {
                cstVideoCall.visible()
                sdvVideoThumb.setRatio(self, 16, 9, CommonUtil.getScreenWidthAsPixel(self) / 2)
                sdvVideoThumb.setImage(thumbnailVideo)
            } ?: cstVideoCall.gone()

            if (!isViCallUser) {
                lblInviteUseViCall.visible()
            } else {
                lblInviteUseViCall.gone()
            }
        }
    }

    private fun call() {
        contactModel?.run {
            if (PermissionUtil.isGranted(
                    self,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    RC_PERMISSION_CALL_PHONE
                )
            ) {
                CommonUtil.call(self, number)
            }
        }
    }

    private fun blockNumber() {
        contactModel?.also { contact ->
            if (isBlocked(contact.number)) {
                presenter.unblockContact(contact)
            } else {
                if (addToBlockList(contact.number)) {
                    toast(String.format(getString(R.string.block_success_), contact.number))
                    lblBlock.text = getString(R.string.unblock)
                }
            }
        }
    }
}