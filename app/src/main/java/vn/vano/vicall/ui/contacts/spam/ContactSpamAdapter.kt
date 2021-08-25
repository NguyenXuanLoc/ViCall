package vn.vano.vicall.ui.contacts.spam

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.setImage
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.data.model.ContactModel

class ContactSpamAdapter(
    private val models: List<ContactModel>,
    private val showInfoListener: (ContactModel) -> Unit,
    private val sendMessageListener: (ContactModel) -> Unit,
    private val removeSpamListener: (ContactModel) -> Unit
) : RecyclerView.Adapter<ContactSpamAdapter.ItemHolder>() {

    private val viewBindHelper by lazy { ViewBinderHelper() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view =
            LayoutInflater.from(parent.ctx).inflate(R.layout.item_contact_spam, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (itemCount > 0) {
            // Bind data
            val model = models[position]
            holder.bind(model)
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdvAvt = itemView.findViewById<SimpleDraweeView>(R.id.sdv_contact_avt)
        private val lblName = itemView.findViewById<TextView>(R.id.lbl_contact_name)
        private val lblPhone = itemView.findViewById<TextView>(R.id.lbl_phone)
        private val imgCallInfo = itemView.findViewById<ImageView>(R.id.img_contact_info)
        private val imgMessage = itemView.findViewById<ImageView>(R.id.img_message)
        private val swipeLayout = itemView.findViewById<SwipeRevealLayout>(R.id.swipe_layout)
        private val btnRemoveSpam = itemView.findViewById<TextView>(R.id.btn_remove_spam)

        fun bind(model: ContactModel) {
            with(model) {
                viewBindHelper.setOpenOnlyOne(true)
                viewBindHelper.bind(swipeLayout, number)
                viewBindHelper.closeLayout(number)

                sdvAvt.setImage(avatar?.let { Uri.parse(it) } ?: R.drawable.ic_mask)
                lblName.text = name ?: number
                lblPhone.text = number

                imgCallInfo.setOnSafeClickListener {
                    showInfoListener(model)
                }

                imgMessage.setOnSafeClickListener {
                    sendMessageListener(model)
                }

                btnRemoveSpam.setOnSafeClickListener {
                    removeSpamListener(model)
                }
            }
        }
    }
}