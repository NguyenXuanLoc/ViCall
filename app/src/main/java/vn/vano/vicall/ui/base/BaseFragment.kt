package vn.vano.vicall.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.jetbrains.anko.toast
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.showApiCallError
import vn.vano.vicall.common.ext.showNetworkError
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.BaseModel

/**
 * A simple [Fragment] subclass.
 */
abstract class BaseFragment<V : BaseView, P : BasePresenterImp<V>> : Fragment(), BaseView {

    protected lateinit var parentActivity: AppCompatActivity
    protected val self: Fragment by lazy { this }
    protected lateinit var presenter: P

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = initPresenter()
        presenter.attachView(initView())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Close keyboard when user touch outside
        CommonUtil.closeKeyboardWhileClickOutSide(parentActivity, view)

        initWidgets()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onApiResponseError(error: BaseModel) {
        ctx?.toast(error.message)
    }

    override fun onApiCallError(e: Throwable?, code: Int) {
        ctx?.showApiCallError(code)
    }

    override fun onNetworkError() {
        ctx?.showNetworkError()
    }

    /*
    * return view
    * */
    abstract fun initView(): V

    /*
    * Return presenter
    * */
    abstract fun initPresenter(): P

    /*
    * return layout id
    * */
    abstract fun getLayoutId(): Int

    /*
    * Set up widgets such as EditText, TextView, RecyclerView, etc
    * */
    abstract fun initWidgets()

}
