package vn.vano.vicall.ui.base

import vn.vano.vicall.common.ErrorCode
import vn.vano.vicall.data.model.BaseModel

interface BaseView {

    fun onApiCallError(e: Throwable? = null, code: Int = ErrorCode.API_ERROR) {}

    fun onApiResponseError(error: BaseModel) {}

    fun onNetworkError() {}

    fun getExtrasValue() {}

    fun dismissRefreshIcon() {}
}