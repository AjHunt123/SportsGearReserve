package com.example.sportsgearreserve.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsgearreserve.R
import com.example.sportsgearreserve.databinding.ItemEquipmentBinding
import com.example.sportsgearreserve.models.Equipment

class EquipmentAdapter(
    private var equipmentList: List<Equipment>,
    private val isStaffMode: Boolean,
    private val onItemClick: (Equipment) -> Unit,
    private val onEditClick: ((Equipment) -> Unit)? = null,
    private val onToggleActiveClick: ((Equipment) -> Unit)? = null,
    private val onDeleteClick: ((Equipment) -> Unit)? = null
) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    inner class EquipmentViewHolder(val binding: ItemEquipmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = ItemEquipmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EquipmentViewHolder(binding)
    }

    override fun getItemCount(): Int = equipmentList.size

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val item = equipmentList[position]
        with(holder.binding) {
            tvEquipmentName.text = item.name
            tvEquipmentCategory.text = "Category: ${item.category}"
            tvEquipmentDescription.text = item.description
            tvTotalStock.text = "Total Quantity: ${item.totalQuantity} units"
            tvMaxLimits.text = "Max ${item.maxPerStudent}/student | Max ${item.maxDurationHours}h"

            val context = root.context
            if (item.isActive) {
                tvActiveBadge.text = "ACTIVE"
                tvActiveBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_active_bg))
                tvActiveBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_active_text))
            } else {
                tvActiveBadge.text = "INACTIVE"
                tvActiveBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_inactive_bg))
                tvActiveBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_inactive_text))
            }

            if (isStaffMode) {
                layoutStaffActions.visibility = View.VISIBLE
                btnToggleActive.text = if (item.isActive) "Deactivate" else "Activate"

                btnEditEquipment.setOnClickListener { onEditClick?.invoke(item) }
                btnToggleActive.setOnClickListener { onToggleActiveClick?.invoke(item) }
                btnDeleteEquipment.setOnClickListener { onDeleteClick?.invoke(item) }
            } else {
                layoutStaffActions.visibility = View.GONE
            }

            root.setOnClickListener { onItemClick(item) }
        }
    }

    fun updateList(newList: List<Equipment>) {
        equipmentList = newList
        notifyDataSetChanged()
    }
}
