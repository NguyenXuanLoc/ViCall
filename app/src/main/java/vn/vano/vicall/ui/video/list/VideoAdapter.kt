package vn.vano.vicall.ui.video.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.NumberUtil
import vn.vano.vicall.data.model.VideoModel

class VideoAdapter(
    private val activity: AppCompatActivity,
    private val models: ArrayList<VideoModel>,
    private val itemClickListener: (VideoModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val margin = CommonUtil.convertDpToPixel(
        activity,
        intArrayOf(R.dimen.dimen_2x, R.dimen.dimen_2x)
    )
    private val screenWidth = CommonUtil.getScreenWidthAsPixel(activity)
    private var totalMargin = margin

    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_NORMAL = 1
    private var isLoaderVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_NORMAL) {
            val view = LayoutInflater.from(parent.ctx).inflate(R.layout.item_video, parent, false)
            ItemHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.ctx).inflate(R.layout.layout_loadmore, parent, false)
            LoadMoreHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemCount > 0) {
            // Bind data
            if (holder is ItemHolder) {
                val model = models[position]
                holder.bind(model)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoaderVisible) {
            when (position) {
                models.size - 1 -> VIEW_TYPE_LOADING
                else -> VIEW_TYPE_NORMAL
            }
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdvThumb = itemView.findViewById<SimpleDraweeView>(R.id.sdv_thumb)
        private val lblPrice = itemView.findViewById<TextView>(R.id.lbl_price)
        private val lblStatus = itemView.findViewById<TextView>(R.id.lbl_status)
        private val lblDownloadCount = itemView.findViewById<TextView>(R.id.lbl_download_count)
        private val lblDuration = itemView.findViewById<TextView>(R.id.lbl_duration)
        private val imgApplied = itemView.findViewById<ImageView>(R.id.img_applied)

        fun bind(model: VideoModel) {
            with(model) {
                sdvThumb.setRatio(activity, 16, 9, totalMargin)
                sdvThumb.setImage(thumbnail)
                if (isWaitingApproved) {
                    lblStatus.visible()
                } else {
                    lblStatus.gone()
                }
                if (isApplied) {
                    imgApplied.visible()
                } else {
                    imgApplied.gone()
                }

                if (isMyVideo) {
                    lblPrice.gone()
                } else {
                    lblPrice.visible()
                    if (price > 0) {
                        lblPrice.textColor = R.color.white
                        lblPrice.text = String.format(
                            activity.getString(R.string._vnd),
                            NumberUtil.formatNumber(price)
                        )
                    } else {
                        lblPrice.textColor = R.color.greenDark
                        lblPrice.text = itemView.ctx.getString(R.string.free)
                    }
                }
                lblDownloadCount.text = NumberUtil.formatNumber(downloadCount)
                lblDuration.text = duration.convertToTimeCounter()

                // Action listeners
                itemView.setOnSafeClickListener {
                    itemClickListener(model)
                }
            }
        }
    }

    inner class LoadMoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun addLoadingView() {
        isLoaderVisible = true
        models.add(VideoModel(null))
        notifyItemInserted(models.size - 1)
    }

    fun removeLoadingView() {
        isLoaderVisible = false
        val position = models.size - 1
        models.removeAt(position)
        notifyItemRemoved(position)
    }

    fun updateThumbRatio(isListView: Boolean) {
        totalMargin = if (isListView) {
            screenWidth / 2 + (margin - margin * 0.25).toInt()
        } else {
            margin
        }
    }
}