package vn.vano.vicall.ui.registerservice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.common.ext.setOnSafeClickListener
import vn.vano.vicall.data.model.RegisterItemModel

class RegisterServiceAdapter(
    private val activity: AppCompatActivity,
    private val registerItems: List<RegisterItemModel>,
    private val itemClickListener: (RegisterItemModel) -> Unit
) : RecyclerView.Adapter<RegisterServiceAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view =
            LayoutInflater.from(parent.ctx).inflate(R.layout.item_register, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return registerItems.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (itemCount > 0) {
            val model = registerItems[position]
            holder.bind(model)
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var btnOption = itemView.findViewById<TextView>(R.id.btn_option)

        fun bind(registerItem: RegisterItemModel) {
            with(registerItem) {
                btnOption.text = registerItem.name
                if (registerItem.order_num == 1) {
                    btnOption.setTextColor(activity.getColor(R.color.white))
                    btnOption.setBackgroundResource(R.drawable.btn_blue_radius)
                }

                btnOption.setOnSafeClickListener {
                    itemClickListener(this)
                }
            }
        }
    }
}

