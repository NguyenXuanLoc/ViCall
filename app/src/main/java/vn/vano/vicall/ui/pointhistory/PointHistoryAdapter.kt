package vn.vano.vicall.ui.pointhistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vn.vano.vicall.R
import vn.vano.vicall.common.ext.ctx
import vn.vano.vicall.data.model.PointItemModel

class PointHistoryAdapter(private val points: ArrayList<PointItemModel>) :
    RecyclerView.Adapter<PointHistoryAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        var view: View =
            LayoutInflater.from(parent.ctx).inflate(R.layout.item_point, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return points.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        if (itemCount > 0) {
            val model = points[position]
            holder.bill(model)
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var lblTitle = itemView.findViewById<TextView>(R.id.lbl_title)
        private var lblContent = itemView.findViewById<TextView>(R.id.lbl_time)
        private var lblPoint = itemView.findViewById<TextView>(R.id.lbl_point)
        fun bill(model: PointItemModel) {
            with(model) {
                lblTitle.text = desc.toString()
                lblContent.text = loyaltyTimeStr.toString()
                lblPoint.text = "+${loyaltyPoint}"
            }
        }
    }
}