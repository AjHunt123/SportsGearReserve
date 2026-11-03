package com.example.sportsgearreserve.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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

            // Status Badge Formatting & Colors
            if (item.isOverdue) {
                tvStatusBadge.text = "OVERDUE"
                tvStatusBadge.setBackgroundColor(Color.parseColor("#FFEBEE"))
                tvStatusBadge.setTextColor(Color.parseColor("#C62828"))
                cardReservation.strokeColor = Color.parseColor("#E53935")
            } else {
                cardReservation.strokeColor = Color.parseColor("#E0E0E0")
                when (item.status) {
                    ReservationStatus.RESERVED -> {
                        tvStatusBadge.text = "RESERVED"
                        tvStatusBadge.setBackgroundColor(Color.parseColor("#E3F2FD"))
                        tvStatusBadge.setTextColor(Color.parseColor("#1976D2"))
                    }
                    ReservationStatus.ISSUED -> {
                        tvStatusBadge.text = "ISSUED"
                        tvStatusBadge.setBackgroundColor(Color.parseColor("#FFF3E0"))
                        tvStatusBadge.setTextColor(Color.parseColor("#E65100"))
                    }
                    ReservationStatus.RETURNED -> {
                        tvStatusBadge.text = "RETURNED"
                        tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"))
                        tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"))
                    }
                    ReservationStatus.CANCELLED -> {
                        tvStatusBadge.text = "CANCELLED"
                        tvStatusBadge.setBackgroundColor(Color.parseColor("#F5F5F5"))
                        tvStatusBadge.setTextColor(Color.parseColor("#757575"))
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
                // Student mode: Allow student to cancel RESERVED before start time (BR-06)
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
