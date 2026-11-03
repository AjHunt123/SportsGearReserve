package com.example.sportsgearreserve.models

import java.io.Serializable

data class Reservation(
    val reservationId: String,
    val userId: String,
    val studentName: String,
    val equipmentId: String,
    val equipmentName: String,
    val quantity: Int,
    val startAt: Long,
    val endAt: Long,
    val status: ReservationStatus = ReservationStatus.RESERVED,
    val createdAt: Long = System.currentTimeMillis(),
    val issuedAt: Long? = null,
    val returnedAt: Long? = null,
    val cancelReason: String? = null
) : Serializable {

    val isOverdue: Boolean
        get() = status == ReservationStatus.ISSUED && endAt < System.currentTimeMillis() && returnedAt == null

    val displayStatus: String
        get() = if (isOverdue) "OVERDUE" else status.name
}
