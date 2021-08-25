package vn.vano.vicall.ui.profile

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
import vn.vano.vicall.data.model.VideoModel

class ProfileVideoPersonalAdapter(
    private val activity: AppCompatActivity,
    private val videoModels: ArrayList<VideoModel>,
    private val itemClickListener: (VideoModel) -> Unit
) :
    RecyclerView.Adapter<ProfileVideoPersonalAdapter.ItemHolder>() {
    private val margin = CommonUtil.convertDpToPixel(
        activity,
        intArrayOf(R.dimen.dimen_2x, R.dimen.dimen_2x, R.dimen.dimen_2x)
    )
    private var totalMargin = (margin + (CommonUtil.getScreenWidthAsPixel(activity) - margin) / 2f).toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view =
            LayoutInflater.from(parent.ctx).inflate(R.layout.item_video_personal, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return videoModels.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (itemCount > 0) {
            holder.bind(videoModels[position])
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdvThumb = itemView.findViewById<SimpleDraweeView>(R.id.sdv_thumb)
        private val imgStatus = itemView.findViewById<ImageView>(R.id.img_status)
        private val lblStatus = itemView.findViewById<TextView>(R.id.lbl_status)
        private val lblDuration = itemView.findViewById<TextView>(R.id.lbl_duration)
        fun bind(model: VideoModel) {
            with(model) {
                lblDuration.text = duration.convertToTimeCounter()
                sdvThumb.setRatio(activity, 16, 9, totalMargin)
                sdvThumb.setImage(thumbnail)
                sdvThumb.setOnSafeClickListener { itemClickListener(model) }
                if (isApplied)
                    imgStatus.visible()
                else if (isWaitingApproved)
                    lblStatus.visible()
            }
        }
    }
}