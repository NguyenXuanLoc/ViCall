package vn.vano.vicall.ui.test

import kotlinx.android.synthetic.main.activity_test.*
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.ui.base.BaseActivity

class TestActivity : BaseActivity<TestView, TestPresenterImp>(), TestView {
    override fun initView(): TestView {
        return this
    }

    override fun initPresenter(): TestPresenterImp {
        return TestPresenterImp(self)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_test
    }

    override fun initWidgets() {
//        presenter.test("OK")
//        presenter.logAction("ssd", "33", "334433", "asds", "", "", "", "");
        btnClick.setOnSafeClickListener {
            presenter.test("OK")
        }
    }
}