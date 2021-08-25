package vn.vano.vicall.ui.home.blockedcall

import android.Manifest
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.DateTime
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.setImage
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.common.util.DateTimeUtil
import vn.vano.vicall.common.util.PermissionUtil
import vn.vano.vicall.data.model.ContactModel
import java.util.*

class CallBlockedAdapter(
    private val models: List<ContactModel>,
    private val showInfoListener: (ContactModel) -> Unit
) : RecyclerView.Adapter<CallBlockedAdapter.ItemHolder>() {

    private val today by lazy { Calendar.getInstance().timeInMillis }
    private var hasReadContactPermission = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        hasReadContactPermission = PermissionUtil.isGranted(
            parent.ctx,
            arrayOf(Manifest.permission.READ_CONTACTS),
            0,
            false
        )

        val view =
            LayoutInflater.from(parent.ctx).inflate(R.layout.item_call_blocked, parent, false)
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
        private val sdvAvt = itemView.findViewById<SimpleDraweeView>(R.id.sdv_caller_avt)
        private val lblName = itemView.findViewById<TextView>(R.id.lbl_caller_name)
        private val lblTime = itemView.findViewById<TextView>(R.id.lbl_time)
        private val imgCallInfo = itemView.findViewById<ImageView>(R.id.img_caller_info)

        fun bind(model: ContactModel) {
            with(model) {
                sdvAvt.setImage(avatar?.let {
                    if (hasReadContactPermission) {
                        Uri.parse(it)
                    } else {
                        R.drawable.ic_mask
                    }
                }, errorImage = R.drawable.ic_mask)
                lblName.text = name ?: number
                date?.also { date ->
                    lblTime.text = String.format(
                        itemView.ctx.getString(R.string.call_time_),
                        DateTimeUtil.convertTimeStampToDate(date, DateTime.Format.HH_MM),
                        DateTimeUtil.getDateDiff(itemView.ctx, date, today)
                    )
                }

                imgCallInfo.setOnSafeClickListener {
                    showInfoListener(model)
                }
            }
        }
    }
}