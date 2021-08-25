package vn.vano.vicall.ui.contacts.allcontact

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.*
import vn.vano.vicall.data.model.ContactModel

class ContactAllAdapter(
    private val models: ArrayList<ContactModel>,
    private val showInfoListener: (ContactModel) -> Unit,
    private val sendMessageListener: (ContactModel) -> Unit,
    private val itemClickListener: (ContactModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_NORMAL = 1
    private var isLoaderVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_NORMAL) {
            val view =
                LayoutInflater.from(parent.ctx).inflate(R.layout.item_contact_all, parent, false)
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
        private val sdvAvt = itemView.findViewById<SimpleDraweeView>(R.id.sdv_contact_avt)
        private val lblName = itemView.findViewById<TextView>(R.id.lbl_contact_name)
        private val lblStatus = itemView.findViewById<TextView>(R.id.lbl_status)
        private val imgViCallBadge = itemView.findViewById<ImageView>(R.id.img_vicall_badge)
        private val imgCallInfo = itemView.findViewById<ImageView>(R.id.img_contact_info)
        private val imgMessage = itemView.findViewById<ImageView>(R.id.img_message)

        fun bind(model: ContactModel) {
            with(model) {
                sdvAvt.setImage(avatar?.let {
                    Uri.parse(it)
                } ?: R.drawable.ic_mask)
                lblName.text = name ?: number

                status?.run {
                    lblStatus.visible()
                    lblStatus.text = this
                } ?: lblStatus.gone()

                // Show ViCall badge if this contact has registered ViCall
                if (isViCallUser) {
                    imgViCallBadge.visible()
                } else {
                    imgViCallBadge.gone()
                }

                // Action listeners
                imgCallInfo.setOnSafeClickListener {
                    showInfoListener(model)
                }

                imgMessage.setOnSafeClickListener {
                    sendMessageListener(model)
                }

                itemView.setOnSafeClickListener {
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