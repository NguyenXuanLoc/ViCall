package vn.vano.vicall.ui.base

import android.content.Context
import io.reactivex.disposables.CompositeDisposable
import vn.vano.vicall.common.ext.networkIsConnected
import vn.vano.vicall.widget.progressbar.MyProgressDialog

open class BasePresenterImp<T : BaseView>(private val context: Context) : BasePresenter<T>() {

    private val progressDialog: MyProgressDialog by lazy { MyProgressDialog(context) }

    protected var view: T? = null
    protected val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    override fun attachView(view: T) {
        view.getExtrasValue()
        this.view = view
    }

    override fun detachView() {
        compositeDisposable.clear()
        view = null
    }

    protected fun showProgressDialog(cancelable: Boolean = false) {
        if (!progressDialog.isShowing) {
            progressDialog.setCancelable(cancelable)
            progressDialog.show()
        }
    }

    protected fun dismissProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    protected fun networkIsAvailable(): Boolean {
        return context.networkIsConnected()
    }
}