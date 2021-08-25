package vn.vano.vicall.ui.notification.detail

import kotlinx.android.synthetic.main.activity_notification_detail.*
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.video.player.PlayerActivity

class NotificationDetailActivity :
    BaseActivity<NotificationDetailView, NotificationDetailPresenterImp>(), NotificationDetailView {

    private var activityItem: ActivityModel? = null

    override fun initView(): NotificationDetailView {
        return this
    }

    override fun initPresenter(): NotificationDetailPresenterImp {
        return NotificationDetailPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_notification_detail
    }

    override fun initWidgets() {
        // Init toolbar
        showTitle(R.string.notification_detail)
        enableHomeAsUp { finish() }

        // Fill detail
        activityItem?.run {
            lblTitle.text = title
            lblTime.text = String.format(getString(R.string.post_time_), createdTimeStr)

            if (content?.startsWith(Constant.HTTP) == true) {
                wvContent.visible()
                wvContent.loadUrl(content)
            } else {
                lblContent.visible()
                lblContent.text = content
            }

            if (hasActionButton) {
                buttonActionUrl?.also { actionUrl ->
                    val component = actionUrl.split("/")
                    val action = component[0]
                    val code = component[1]

                    btnAction.text = buttonText
                    btnAction.setOnSafeClickListener {
                        when (action) {
                            Constant.SMS_VIDEO -> {
                                CommonUtil.composeSms(
                                    self,
                                    Constant.SERVICE_PHONE_NUMBER, "${Constant.BUY} $code"
                                )
                            }
                            Constant.SMS_REG -> {
                                CommonUtil.composeSms(
                                    self,
                                    Constant.SERVICE_PHONE_NUMBER, "${Constant.DK} $code"
                                )
                            }
                            Constant.VIDEO_STORE -> {
                                videoLink?.also {
                                    val videos = arrayListOf<VideoModel>()
                                    videos.add(VideoModel(code).apply {
                                        fileName = videoLink
                                    })
                                    PlayerActivity.start(ctx, videos, 0)
                                }
                            }
                        }
                    }
                }
            } else {
                btnAction.gone()
            }
        }
    }

    override fun getExtrasValue() {
        intent.extras?.run {
            if (containsKey(Key.ACTIVITY_MODEL)) {
                activityItem = getSerializable(Key.ACTIVITY_MODEL) as ActivityModel?
            }
        }
    }
}