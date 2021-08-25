package vn.vano.vicall.widget

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vn.vano.vicall.common.Constant

abstract class PaginationScrollListener(lm: LinearLayoutManager? = null) :
    RecyclerView.OnScrollListener() {

    private var layoutManager: LinearLayoutManager? = lm

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        layoutManager?.also { layoutManager ->
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

            if (!isLoading() && !isLastPage()) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= Constant.PAGE_SIZE
                ) {
                    loadMoreItems()
                }
            }
        }
    }

    abstract fun loadMoreItems()

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean

    fun setLayoutManager(layoutManager: LinearLayoutManager) {
        this.layoutManager = layoutManager
    }
}