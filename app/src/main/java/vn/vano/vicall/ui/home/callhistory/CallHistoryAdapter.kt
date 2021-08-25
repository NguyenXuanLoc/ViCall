package vn.vano.vicall.ui.home.callhistory

import android.Manifest
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.DateTime
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.common.util.DateTimeUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import java.util.*

class CallHistoryAdapter(
    private val models: ArrayList<ContactModel>,
    private val showInfoListener: (ContactModel) -> Unit,
    private val sendMessageListener: (ContactModel) -> Unit,
    private val reportSpamListener: (ContactModel) -> Unit,
    private val itemClickListener: (ContactModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewBindHelper by lazy { ViewBinderHelper() }
    private val today by lazy { Calendar.getInstance().timeInMillis }
    private var hasReadContactPermission = false

    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_NORMAL = 1
    private var isLoaderVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        hasReadContactPermission = PermissionUtil.isGranted(
            parent.ctx,
            arrayOf(Manifest.permission.READ_CONTACTS),
            0,
            false
        )

        return if (viewType == VIEW_TYPE_NORMAL) {
            val view =
                LayoutInflater.from(parent.ctx).inflate(R.layout.item_call_history, parent, false)
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
        private val cstItemMain = itemView.findViewById<ConstraintLayout>(R.id.cst_item_main)
        private val sdvAvt = itemView.findViewById<SimpleDraweeView>(R.id.sdv_caller_avt)
        private val imgViCallBadge = itemView.findViewById<ImageView>(R.id.img_vicall_badge)
        private val lblName = itemView.findViewById<TextView>(R.id.lbl_caller_name)
        private val lblStatus = itemView.findViewById<TextView>(R.id.lbl_status)
        private val lblTime = itemView.findViewById<TextView>(R.id.lbl_time)
        private val imgCallType = itemView.findViewById<ImageView>(R.id.img_call_type)
        private val imgCallInfo = itemView.findViewById<ImageView>(R.id.img_caller_info)
        private val imgMessage = itemView.findViewById<ImageView>(R.id.img_message)
        private val swipeLayout = itemView.findViewById<SwipeRevealLayout>(R.id.swipe_layout)
        private val btnReportSpam = itemView.findViewById<TextView>(R.id.btn_report_spam)

        fun bind(model: ContactModel) {
            with(model) {
                viewBindHelper.setOpenOnlyOne(true)
                viewBindHelper.bind(swipeLayout, date.toString())
                viewBindHelper.closeLayout(date.toString())

                sdvAvt.setImage(avatar?.let {
                    if (hasReadContactPermission) {
                        Uri.parse(it)
                    } else {
                        R.drawable.ic_mask
                    }
                }, errorImage = R.drawable.ic_mask)
                if (isViCallUser) {
                    imgViCallBadge.visible()
                } else {
                    imgViCallBadge.gone()
                }

                lblName.text = if (name?.isNotBlank() == true) {
                    name
                } else {
                    number
                }
                if (status?.isNotBlank() == true) {
                    lblStatus.visible()
                    lblStatus.text = status
                } else {
                    lblStatus.gone()
                }
                date?.also { date ->
                    lblTime.text = String.format(
                        itemView.ctx.getString(R.string.call_time_),
                        DateTimeUtil.convertTimeStampToDate(date, DateTime.Format.HH_MM),
                        DateTimeUtil.getDateDiff(itemView.ctx, date, today)
                    )
                }
                callType?.run {
                    imgCallType.visible()
                    imgCallType.setImageResource(
                        when {
                            isIncomingCall() -> {
                                R.drawable.ic_incoming_call
                            }
                            isMissedCall() -> {
                                if (isSpam) {
                                    R.drawable.ic_spam_call
                                } else {
                                    R.drawable.ic_missed_call
                                }
                            }
                            isOutgoingCall() -> R.drawable.ic_outgoing_call
                            isRejectedCall() -> R.drawable.ic_rejected_call
                            isBlockedCall() -> R.drawable.ic_blocked_call
                            else -> R.drawable.ic_missed_call
                        }
                    )
                } ?: imgCallType.gone()

                // Action listeners
                imgCallInfo.setOnSafeClickListener {
                    showInfoListener(model)
                }

                imgMessage.setOnSafeClickListener {
                    sendMessageListener(model)
                }

                btnReportSpam.setOnSafeClickListener {
                    reportSpamListener(model)
                }

                cstItemMain.setOnSafeClickListener {
                    itemClickListener(model)
                }
            }
        }
    }

    inner class LoadMoreHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun addLoadingView() {
        isLoaderVisible = true
        models.add(ContactModel(null))
        notifyItemInserted(models.size - 1)
    }

    fun removeLoadingView() {
        isLoaderVisible = false
        val position = models.size - 1
        models.removeAt(position)
        notifyItemRemoved(position)
    }
}