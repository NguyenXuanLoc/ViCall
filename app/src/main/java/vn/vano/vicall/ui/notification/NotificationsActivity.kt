package vn.vano.vicall.ui.notification

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_notifications.*
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.ctx
import org.jetbrains.anko.toast
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.openActivity
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.data.model.ActivityModel
import vn.vano.vicall.ui.base.BaseActivity
import vn.vano.vicall.ui.notification.detail.NotificationDetailActivity
import vn.vano.vicall.ui.webview.WebviewActivity
import vn.vano.vicall.widget.PaginationScrollListener

class NotificationsActivity : BaseActivity<NotificationsView, NotificationsPresenterImp>(),
    NotificationsView, SwipeRefreshLayout.OnRefreshListener {

    private val activitys by lazy { ArrayList<ActivityModel>() }
    private val adapter by lazy {
        NotificationsAdapter(activitys) {
            showActivityDetail(it)
        }
    }

    private var currentPage = 1
    private var hasMoreData = true
    private var isLoading = false
    private val paginationScrollListener by lazy {
        object : PaginationScrollListener() {
            override fun loadMoreItems() {
                isLoading = true
                currentPage++
                rclAnnouncement.post {
                    adapter.addLoadingView()
                }
                getAnnouncement(false)
            }

            override fun isLastPage(): Boolean {
                return !hasMoreData
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        }
    }

    override fun initView(): NotificationsView {
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getAnnouncement(true)
    }

    override fun initPresenter(): NotificationsPresenterImp {
        return NotificationsPresenterImp(this)
    }

    override fun getLayoutId(): Int? {
        return R.layout.activity_notifications
    }

    override fun initWidgets() {
        showTitle(R.string.notification)
        enableHomeAsUp { finish() }

        swrNotifications.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
        swrNotifications.setOnRefreshListener(this)
        rclAnnouncement.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclAnnouncement.setHasFixedSize(true)
        rclAnnouncement.addItemDecoration(
            DividerItemDecoration(
                ctx,
                DividerItemDecoration.VERTICAL
            )
        )
        rclAnnouncement.adapter = adapter
        setPaginationLayoutManager()
        rclAnnouncement.addOnScrollListener(paginationScrollListener)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notifications, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_read_all -> {
                if (activitys.size > 0) {
                    currentUser?.phoneNumberNonPrefix?.run {
                        presenter.readNotifyAll(this)
                    }
                } else {
                    toast(R.string.not_notification)
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAnnouncementLoaded(result: List<ActivityModel>) {
        if (currentPage == 1) {
            this.activitys.clear()
        } else {
            adapter.removeLoadingView()
        }
        this.activitys.addAll(result)
        this.adapter.notifyDataSetChanged()

        if (activitys.isEmpty()) {
            lblNotification.visible()
        } else {
            lblNotification.gone()
        }
    }

    override fun checkNull(boolean: Boolean) {
        isLoading = boolean
    }

    override fun onNotificationMarkedReadSuccess() {
        pushUserChangedEvent()
    }

    override fun readNotifyAllSuccess() {
        toast(getString(R.string.read_notify_all_success))

        // Update adapter
        for (index in activitys.indices) {
            if (!activitys[index].isRead) {
                activitys[index].isRead = true
                adapter.notifyItemChanged(index)
            }
        }

        // Push number of unread notification changed event
        pushUserChangedEvent(0)
    }

    override fun onRefresh() {
        currentPage = 1
        hasMoreData = false
        getAnnouncement(false)
    }

    override fun dismissRefreshIcon() {
        swrNotifications.isRefreshing = false
    }

    private fun getAnnouncement(showProgress: Boolean) {
        currentUser?.phoneNumberNonPrefix?.run {
            presenter.getAnnouncement(
                this,
                currentPage.toString(),
                Constant.PAGE_SIZE.toString(),
                showProgress
            )
        }
    }

    private fun showActivityDetail(activityModel: ActivityModel?) {
        activityModel?.run {
            // Mark as read
            if (!isRead) {
                presenter.readNotify(id.toString())
            }

            // Open detail page
            when (actionType) {
                Constant.NOTIFY, Constant.NOTIFY_SELF_APP, Constant.REG_SERVICE, Constant.UNREG_SERVICE -> {
                    bundleOf(Key.ACTIVITY_MODEL to activityModel).run {
                        openActivity(NotificationDetailActivity::class.java, this)
                    }
                }
                Constant.NOTIFY_HTML -> {
                    if (hasActionButton) {
                        buttonActionUrl?.run {
                            WebviewActivity.start(ctx, this, true)
                        }
                    } else {
                        content?.run {
                            WebviewActivity.start(ctx, this)
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun setPaginationLayoutManager() {
        paginationScrollListener.setLayoutManager(rclAnnouncement.layoutManager as LinearLayoutManager)
    }

    private fun pushUserChangedEvent(unread: Int? = null) {
        currentUser?.run {
            unreadNotification = unread ?: (unreadNotification - 1)
            CommonUtil.saveLoggedInUserToList(ctx, this)
        }
    }
}