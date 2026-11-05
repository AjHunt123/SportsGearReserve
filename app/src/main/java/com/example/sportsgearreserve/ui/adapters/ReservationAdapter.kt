package com.example.sportsgearreserve.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sportsgearreserve.R
import com.example.sportsgearreserve.databinding.ItemReservationBinding
import com.example.sportsgearreserve.models.Reservation
import com.example.sportsgearreserve.models.ReservationStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservationAdapter(
    private var reservationList: List<Reservation>,
    private val isStaffMode: Boolean,
    private val onIssueClick: ((Reservation) -> Unit)? = null,
    private val onReturnClick: ((Reservation) -> Unit)? = null,
    private val onCancelClick: ((Reservation) -> Unit)? = null
) : RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

    inner class ReservationViewHolder(val binding: ItemReservationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReservationViewHolder(binding)
    }

    override fun getItemCount(): Int = reservationList.size

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val item = reservationList[position]
        with(holder.binding) {
            tvEquipmentName.text = item.equipmentName
            tvStudentInfo.text = "Student: ${item.studentName}"
            tvQuantity.text = "Reserved Qty: ${item.quantity} unit(s)"
            tvReservationId.text = "ID: ${item.reservationId}"

            val startStr = dateFormat.format(Date(item.startAt))
            val endStr = dateFormat.format(Date(item.endAt))
            tvTimeWindow.text = "Window: $startStr – $endStr"

            val context = root.context

            // Status Badge Formatting & Colors
            if (item.isOverdue) {
                tvStatusBadge.text = "OVERDUE"
                tvStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_overdue_bg))
                tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_overdue_text))
                cardReservation.strokeColor = ContextCompat.getColor(context, R.color.badge_overdue_border)
            } else {
                cardReservation.strokeColor = ContextCompat.getColor(context, R.color.card_stroke)
                when (item.status) {
                    ReservationStatus.RESERVED -> {
                        tvStatusBadge.text = "RESERVED"
                        tvStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_reserved_bg))
                        tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_reserved_text))
                    }
                    ReservationStatus.ISSUED -> {
                        tvStatusBadge.text = "ISSUED"
                        tvStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_issued_bg))
                        tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_issued_text))
                    }
                    ReservationStatus.RETURNED -> {
                        tvStatusBadge.text = "RETURNED"
                        tvStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_active_bg))
                        tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_active_text))
                    }
                    ReservationStatus.CANCELLED -> {
                        tvStatusBadge.text = "CANCELLED"
                        tvStatusBadge.setBackgroundColor(ContextCompat.getColor(context, R.color.badge_inactive_bg))
                        tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_inactive_text))
                    }
                }
            }

            // Cancellation Reason Display
            if (item.status == ReservationStatus.CANCELLED && !item.cancelReason.isNullOrBlank()) {
                tvCancelReason.visibility = View.VISIBLE
                tvCancelReason.text = "Reason: ${item.cancelReason}"
            } else {
                tvCancelReason.visibility = View.GONE
            }

            // Action Buttons Visibility
            btnIssue.visibility = View.GONE
            btnReturn.visibility = View.GONE
            btnCancel.visibility = View.GONE
            layoutActions.visibility = View.GONE

            if (isStaffMode) {
                if (item.status == ReservationStatus.RESERVED) {
                    layoutActions.visibility = View.VISIBLE
                    btnIssue.visibility = View.VISIBLE
                    btnCancel.visibility = View.VISIBLE

                    btnIssue.setOnClickListener { onIssueClick?.invoke(item) }
                    btnCancel.setOnClickListener { onCancelClick?.invoke(item) }
                } else if (item.status == ReservationStatus.ISSUED) {
                    layoutActions.visibility = View.VISIBLE
                    btnReturn.visibility = View.VISIBLE
                    btnReturn.setOnClickListener { onReturnClick?.invoke(item) }
                }
            } else {
                // Student mode: Allow student to cancel RESERVED before start time
                val now = System.currentTimeMillis()
                if (item.status == ReservationStatus.RESERVED && item.startAt > now) {
                    layoutActions.visibility = View.VISIBLE
                    btnCancel.visibility = View.VISIBLE
                    btnCancel.setOnClickListener { onCancelClick?.invoke(item) }
                }
            }
        }
    }

    fun updateList(newList: List<Reservation>) {
        reservationList = newList
        notifyDataSetChanged()
    }
}
