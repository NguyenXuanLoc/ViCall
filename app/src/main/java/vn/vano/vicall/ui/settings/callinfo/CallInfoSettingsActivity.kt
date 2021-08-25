package vn.vano.vicall.ui.settings.callinfo

import kotlinx.android.synthetic.main.activity_callinfo_settings.*
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.SettingsModel
import vn.vano.vicall.ui.base.BaseActivity

class CallInfoSettingsActivity : BaseActivity<CallInfoSettingsView, CallInfoSettingsPresenterImp>(),
    CallInfoSettingsView {

    override fun onStop() {
        // Save settings to shared pref before exiting
        CommonUtil.saveSettings(ctx, SettingsModel().apply {
            callerVideoIsOn = swchVideo.isChecked
            videoSoundIsOn = chkVideoSound.isChecked
        })
        super.onStop()
    }

    override fun initView(): CallInfoSettingsView {
        return this
    }

    override fun initPresenter(): CallInfoSettingsPresenterImp {
        return CallInfoSettingsPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_callinfo_settings
    }

    override fun initWidgets() {
        // Init toolbar
        showTitle(R.string.setup_incoming_video)
        enableHomeAsUp { finish() }

        // Listener
        swchVideo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chkVideoSound.visible()
            } else {
                chkVideoSound.gone()
            }
        }

        // Fill user's settings
        CommonUtil.getSettings(ctx).run {
            swchVideo.isChecked = callerVideoIsOn
            chkVideoSound.isChecked = videoSoundIsOn
        }
    }
}