package vn.vano.vicall.ui.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.Constant
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.data.model.ActivityModel

class NotificationsAdapter(
    private val activitys: ArrayList<ActivityModel>,
    private val itemClickListener: (ActivityModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_NORMAL = 1
    private var isLoaderVisible = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_NORMAL) {
            val view =
                LayoutInflater.from(parent.ctx).inflate(R.layout.item_annoumcement, parent, false)
            ItemHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.ctx).inflate(R.layout.layout_loadmore, parent, false)
            LoadMoreHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoaderVisible) {
            when (position) {
                activitys.size - 1 -> VIEW_TYPE_LOADING
                else -> VIEW_TYPE_NORMAL
            }
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun getItemCount(): Int {
        return activitys.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (itemCount > 0) {
            // Bind data
            if (holder is NotificationsAdapter.ItemHolder) {
                val model = activitys[position]
                holder.bind(model)
            }
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdvAnnouncement = itemView.findViewById<SimpleDraweeView>(R.id.sdv_notification)
        private val lblTitle = itemView.findViewById<TextView>(R.id.lbl_title)
        private val lblContent = itemView.findViewById<TextView>(R.id.lbl_content)
        private val lblTime = itemView.findViewById<TextView>(R.id.lbl_time)

        fun bind(activityModel: ActivityModel) {
            with(activityModel) {
                lblTitle.text = title
                lblContent.text = content
                val splitTime = createdTimeStr?.split(",")
                lblTime.text = "${splitTime?.get(0)}, \n ${splitTime?.get(1)}"
                when (iconType) {
                    Constant.VICALL -> {
                        changeIcon(
                            R.drawable.bg_circle_blue,
                            R.drawable.ic_call_id_white,
                            sdvAnnouncement
                        )
                    }
                    Constant.CANCEL -> {
                        changeIcon(
                            R.drawable.bg_circle_red,
                            R.drawable.ic_clear_while,
                            sdvAnnouncement
                        )
                    }
                    Constant.EVENT, Constant.SMS -> {
                        changeIcon(
                            R.drawable.bg_circle_blue,
                            R.drawable.ic_call_id_white,
                            sdvAnnouncement
                        )
                    }
                }
                if (isRead) {
                    lblTitle.setTextAppearance(R.style.TextView)
                } else {
                    lblTitle.setTextAppearance(R.style.TextView_Bold)
                }
                itemView.setOnSafeClickListener {
                    lblTitle.setTextAppearance(R.style.TextView)
                    itemClickListener(this)
                }
            }
        }
    }

    inner class LoadMoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun changeIcon(background: Int, icon: Int, sdvAnnouncement: SimpleDraweeView) {
        sdvAnnouncement.setBackgroundResource(background)
        sdvAnnouncement.setImageResource(icon)
    }

    fun addLoadingView() {
        isLoaderVisible = true
        activitys.add(ActivityModel())
        notifyItemInserted(activitys.size - 1)
    }

    fun removeLoadingView() {
        isLoaderVisible = false
        val position = activitys.size - 1
        activitys.removeAt(position)
        notifyItemRemoved(position)
    }
}
