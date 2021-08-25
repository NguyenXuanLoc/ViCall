package vn.vano.vicall.ui.pointhistory

import vn.vano.vicall.data.model.PointModel
import vn.vano.vicall.ui.base.BaseView

interface PointHistoryView : BaseView {
    fun onPointLoaded(result: PointModel)
}