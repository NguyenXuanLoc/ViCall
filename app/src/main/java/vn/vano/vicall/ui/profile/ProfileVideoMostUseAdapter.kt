package vn.vano.vicall.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.CommonUtil
import vn.vano.vicall.common.util.NumberUtil
import vn.vano.vicall.data.model.VideoModel

class ProfileVideoMostUseAdapter(
    private val activity: AppCompatActivity,
    private var videos: ArrayList<VideoModel>,
    private val itemClickListener: (VideoModel) -> Unit
) : RecyclerView.Adapter<ProfileVideoMostUseAdapter.ItemHolder>() {

    private val margin = CommonUtil.convertDpToPixel(
        activity,
        intArrayOf(R.dimen.dimen_2x, R.dimen.dimen_2x, R.dimen.dimen_2x)
    )
    private var totalMargin =
        (margin + (CommonUtil.getScreenWidthAsPixel(activity) - margin) / 2f).toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        var view =
            LayoutInflater.from(parent.ctx).inflate(R.layout.item_video_most_use, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (itemCount > 0) {
            val model = videos[position]
            holder.bind(model)
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdvThumb = itemView.findViewById<SimpleDraweeView>(R.id.sdv_thumb)
        private val lblDuration = itemView.findViewById<TextView>(R.id.lbl_duration)
        private val lblDownLoadCount = itemView.findViewById<TextView>(R.id.lbl_download_count)
        private val lblPrice = itemView.findViewById<TextView>(R.id.lbl_price)
        fun bind(video: VideoModel) {
            with(video) {
                sdvThumb.setRatio(activity, 16, 9, totalMargin)
                sdvThumb.setImage(thumbnail)
                lblDuration.text = duration.convertToTimeCounter()
                lblDownLoadCount.text = downloadCount.toString()
                if (video.price > 0) {
                    lblPrice.textColor = R.color.white
                    lblPrice.text = String.format(
                        activity.getString(R.string._vnd),
                        NumberUtil.formatNumber(video.price)
                    )
                } else {
                    lblPrice.textColor = R.color.greenDark
                    lblPrice.text = itemView.ctx.getString(R.string.free)
                }
                sdvThumb.setOnSafeClickListener { itemClickListener(video) }
            }
        }
    }
}