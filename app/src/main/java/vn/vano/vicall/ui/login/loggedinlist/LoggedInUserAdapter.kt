package vn.vano.vicall.ui.login.loggedinlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.setImage
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.data.model.UserModel

class LoggedInUserAdapter(
    private val users: List<UserModel>,
    private val itemClickListener: (UserModel) -> Unit
) : RecyclerView.Adapter<LoggedInUserAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view =
            LayoutInflater.from(parent.ctx).inflate(R.layout.item_logged_in_user, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (itemCount > 0) {
            val model = users[position]
            holder.bind(model)
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdvAvt = itemView.findViewById<SimpleDraweeView>(R.id.sdv_avt)
        private val lblName = itemView.findViewById<TextView>(R.id.lbl_name)
        private val lblPhone = itemView.findViewById<TextView>(R.id.lbl_phone)

        fun bind(userModel: UserModel) {
            with(userModel) {
                sdvAvt.setImage(avatar)
                lblName.text = name
                lblPhone.text = phoneNumberNonPrefix

                itemView.setOnSafeClickListener {
                    itemClickListener(this)
                }
            }
        }
    }
}