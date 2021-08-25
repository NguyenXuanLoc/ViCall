package vn.vano.vicall.ui.splash

import android.os.Bundle
import com.google.gson.Gson
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.convertMapToJSONObject
import vn.vano.vicall.common.ext.createNotificationIntent
import vn.vano.vicall.common.ext.openActivity
import vn.vano.vicall.data.mapper.convertToModel
import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.data.response.ActivityResponse
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.introduction.IntroSliderActivity
import vn.vano.vicall.ui.login.LoginActivity
import vn.vano.vicall.ui.main.MainActivity

class SplashActivity : BaseActivity<SplashView, SplashPresenterImp>(), SplashView {

    private var activityNotificationModel: ActivityModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.delay()
    }

    override fun getExtrasValue() {
        intent?.extras?.also { bundle ->
            if (bundle.containsKey("actionType")) {
                val map = HashMap<String, Any?>()
                for (key in bundle.keySet()) {
                    map[key] = bundle.get(key)
                }
                val jsonObject = convertMapToJSONObject(map)
                val activityResponse =
                    Gson().fromJson(jsonObject.toString(), ActivityResponse::class.java)
                activityNotificationModel = activityResponse.convertToModel()
            }
        }
    }

    override fun initView(): SplashView {
        return this
    }

    override fun initPresenter(): SplashPresenterImp {
        return SplashPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_splash
    }

    override fun initWidgets() {
        // This screen doesn't use toolbar
        hideToolbarBase()
    }

    override fun openLoginPage() {
        openActivity(LoginActivity::class.java)
    }

    override fun openHomePage() {
        openActivity(MainActivity::class.java)
        openNotificationDetail()
    }

    override fun openIntroductionPage() {
        openActivity(IntroSliderActivity::class.java)
    }

    override fun finishSplash() {
        finish()
    }

    private fun openNotificationDetail() {
        activityNotificationModel?.run {
            try {
                val intent = createNotificationIntent(this)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
