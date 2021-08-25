package vn.vano.vicall.ui.pointhistory

import android.os.Bundle
import android.view.Menu
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_point_history.*
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.data.model.PointItemModel
import vn.vano.vicall.data.model.PointModel
import vn.vano.vicall.ui.base.BaseActivity

class PointHistoryActivity : BaseActivity<PointHistoryView, PointHistoryPresenterImp>(),
    PointHistoryView {
    private val itemPoints by lazy { ArrayList<PointItemModel>() }
    private val adapter by lazy { PointHistoryAdapter(itemPoints) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUser?.phoneNumberNonPrefix?.run {
            presenter.getPointHistory(this)
        }
    }

    override fun initView(): PointHistoryView {
        return this
    }

    override fun initPresenter(): PointHistoryPresenterImp {
        return PointHistoryPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_point_history
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.point_history_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun initWidgets() {
        showTitle(R.string.point_history)
        enableHomeAsUp { finish() }
        rclPoint.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclPoint.setHasFixedSize(true)
        rclPoint.adapter = adapter
        // Event exchange gift
        btnExchangeGift.setOnSafeClickListener { }
    }

    override fun onPointLoaded(result: PointModel) {
        lblTotalPoint.text = result.totalPoint.toString()
        itemPoints.addAll(result.lstLoyalty!!)
        adapter.notifyDataSetChanged()
    }
}