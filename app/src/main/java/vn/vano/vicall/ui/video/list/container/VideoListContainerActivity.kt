package vn.vano.vicall.ui.video.list.container

import android.os.Bundle
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.addFragment
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.video.list.VideoListFragment

class VideoListContainerActivity : BaseActivity<ContainerView, ContainerPresenterImp>(),
    ContainerView {

    private var type: String? = null
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type?.run {
            addFragment(
                R.id.frl_video_list_container,
                VideoListFragment.newInstance(this, category)
            )
        }
    }

    override fun getExtrasValue() {
        intent?.extras?.run {
            if (containsKey(Key.TYPE)) {
                type = getString(Key.TYPE)
            }
            if (containsKey(Key.CATEGORY)) {
                category = getString(Key.CATEGORY)
            }
        }
    }

    override fun initView(): ContainerView {
        return this
    }

    override fun initPresenter(): ContainerPresenterImp {
        return ContainerPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_video_list_container
    }

    override fun initWidgets() {
        // Init toolbar
        when (category) {
            Constant.MOST_USED -> showTitle(R.string.used_most)
            Constant.LASTEST -> showTitle(R.string.newest)
            Constant.FREE -> showTitle(R.string.free_vi)
        }

        enableHomeAsUp {
            finish()
        }
    }
}
