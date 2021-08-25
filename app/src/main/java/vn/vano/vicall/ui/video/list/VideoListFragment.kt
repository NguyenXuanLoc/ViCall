package vn.vano.vicall.ui.video.list

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_video_list.*
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.Key
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.currentUser
import vn.vano.vicall.common.ext.gone
import vn.vano.vicall.common.ext.visible
import vn.vano.vicall.data.model.VideoModel
import vn.vano.vicall.ui.base.BaseFragment
import vn.vano.vicall.ui.video.player.PlayerActivity
import vn.vano.vicall.widget.PaginationScrollListener

class VideoListFragment : BaseFragment<VideoListView, VideoListPresenterImp>(), VideoListView,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private const val RC_DELETING_VIDEO = 256
        private const val ARG_TYPE = "arg_type"
        private const val ARG_CATEGORY = "arg_category"

        fun newInstance(type: String, category: String? = null): VideoListFragment {
            return VideoListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                    putString(ARG_CATEGORY, category)
                }
            }
        }
    }

    private var isViewed = false
    private var isListViewLayout = true
    private lateinit var type: String
    private var category: String? = null
    private val videos by lazy { ArrayList<VideoModel>() }
    private val adapter by lazy {
        VideoAdapter(parentActivity, videos) {
            ctx?.run {
                PlayerActivity.start(this, videos, videos.indexOf(it), RC_DELETING_VIDEO, self)
            }
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
                rclVideo.post {
                    adapter.addLoadingView()
                }
                getVideos(false)
            }

            override fun isLastPage(): Boolean {
                return !hasMoreData
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_DELETING_VIDEO && resultCode == RESULT_OK) {
            data?.extras?.getInt(Key.INDEX)?.run {
                // Remove video which deleted from Player page
                videos.removeAt(this)
                adapter.notifyItemRemoved(this)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getString(ARG_TYPE, "")
            category = it.getString(ARG_CATEGORY)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isViewed) {
            getVideos(true)
            isViewed = true
        }
    }

    override fun onDestroyView() {
        removePaginationListener()
        super.onDestroyView()
    }

    override fun initView(): VideoListView {
        return this
    }

    override fun initPresenter(): VideoListPresenterImp {
        return VideoListPresenterImp(ctx!!)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_video_list
    }

    override fun initWidgets() {
        swpVideo.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
        )
        swpVideo.setOnRefreshListener(this)
        rclVideo.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        rclVideo.setHasFixedSize(true)
        rclVideo.adapter = adapter
        setPaginationLayoutManager()
        rclVideo.addOnScrollListener(paginationScrollListener)

        // Listeners
        presenter.listenLayoutChanged()
    }

    override fun onVideosLoadedSuccess(videos: List<VideoModel>) {
        // Refresh adapter
        if (currentPage == 1) { // Reset old data if current page = 1(the first call or user refreshes the list)
            this.videos.clear()
        } else {
            adapter.removeLoadingView()
        }
        this.videos.addAll(videos)
        adapter.notifyDataSetChanged()

        // Update pagination flags
        hasMoreData = videos.size == Constant.PAGE_SIZE
        isLoading = false

        // Show/hide 'no data' layout
        toggleNoDataLayout()
    }

    override fun onLayoutChanged() {
        if (isListViewLayout) {
            rclVideo.layoutManager = GridLayoutManager(ctx, 2, GridLayoutManager.VERTICAL, false)
        } else {
            rclVideo.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false)
        }
        adapter.updateThumbRatio(isListViewLayout)
        adapter.notifyDataSetChanged()
        setPaginationLayoutManager()

        // Update layout flag
        isListViewLayout = !isListViewLayout
    }

    override fun onRefresh() {
        currentPage = 1
        hasMoreData = false
        getVideos(false)
    }

    override fun dismissRefreshIcon() {
        swpVideo.isRefreshing = false
    }

    private fun getVideos(showProgress: Boolean) {
        val phone = if (type == Constant.PERSONAL) {
            ctx?.currentUser?.phoneNumberNonPrefix
        } else {
            null
        }
        presenter.getVideos(type, category, phone, currentPage, showProgress)
    }

    private fun toggleNoDataLayout() {
        if (videos.isNotEmpty()) {
            rclVideo.visible()
            lblNoData.gone()
        } else {
            rclVideo.gone()
            lblNoData.visible()
        }
    }

    private fun removePaginationListener() {
        rclVideo.removeOnScrollListener(paginationScrollListener)
    }

    private fun setPaginationLayoutManager() {
        paginationScrollListener.setLayoutManager(rclVideo.layoutManager as LinearLayoutManager)
    }
}
