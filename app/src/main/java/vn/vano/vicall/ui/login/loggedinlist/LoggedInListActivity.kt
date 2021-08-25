package vn.vano.vicall.ui.login.loggedinlist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_logged_in_list.*
import org.jetbrains.anko.ctx
import vn.vano.vicall.R
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.data.model.UserModel
import vn.vano.vicall.ui.base.BaseActivity

class LoggedInListActivity : BaseActivity<LoggedInListView, LoggedInListPresenterImp>(),
    LoggedInListView {

    private val users by lazy { ArrayList<UserModel>() }
    private val adapter by lazy {
        LoggedInUserAdapter(users) {
            setResult(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get logged in users from shared pref
        presenter.getLoggedInUsers()
    }

    override fun initView(): LoggedInListView {
        return this
    }

    override fun initPresenter(): LoggedInListPresenterImp {
        return LoggedInListPresenterImp(ctx)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_logged_in_list
    }

    override fun initWidgets() {
        // Init toolbar base
        hideToolbarBase()

        // Init recyclerview
        rclUser.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclUser.setHasFixedSize(true)
        rclUser.adapter = adapter

        // Use other account
        btnUseOtherAccount.setOnSafeClickListener {
            setResult(null)
        }
    }

    override fun onUsersLoadedSuccess(users: List<UserModel>) {
        this.users.addAll(users)
        adapter.notifyDataSetChanged()
    }

    private fun setResult(user: UserModel?) {
        // Set result
        Intent().apply {
            putExtra(Key.USER_MODEL, user)
        }.run {
            setResult(Activity.RESULT_OK, this)
        }

        // Close activity
        finish()
    }
}
