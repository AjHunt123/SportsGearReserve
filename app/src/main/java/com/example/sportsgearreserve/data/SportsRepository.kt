package com.example.sportsgearreserve.data

import com.example.sportsgearreserve.models.Equipment
import com.example.sportsgearreserve.models.Reservation
import com.example.sportsgearreserve.models.ReservationStatus
import com.example.sportsgearreserve.models.User
import com.example.sportsgearreserve.models.UserRole
import java.util.UUID

object SportsRepository {

    private val users = mutableListOf<User>()
    private val equipmentList = mutableListOf<Equipment>()
    private val reservations = mutableListOf<Reservation>()

    init {
        seedMockData()
        syncWithFirebase()
    }

    private fun seedMockData() {
        // Users
        val student1 = User(
            userId = "std_1",
            prefix = "Mr.",
            firstName = "Aljohn",
            middleName = "",
            lastName = "Fornolles",
            email = "student@uc.edu.ph",
            studentId = "17425546",
            role = UserRole.STUDENT
        )
        val student2 = User(
            userId = "std_2",
            prefix = "Ms.",
            firstName = "Maria",
            middleName = "Clara",
            lastName = "Santos",
            email = "maria@uc.edu.ph",
            studentId = "17425001",
            role = UserRole.STUDENT
        )
        val staff1 = User(
            userId = "staff_1",
            prefix = "Mr.",
            firstName = "Sports Office",
            middleName = "",
            lastName = "Admin",
            email = "staff@uc.edu.ph",
            studentId = null,
            role = UserRole.STAFF
        )
        users.addAll(listOf(student1, student2, staff1))

        // Equipment
        val eq1 = Equipment(
            equipmentId = "eq_1",
            name = "Basketball (Molten GG7X)",
            category = "Balls",
            description = "Official size 7 indoor/outdoor synthetic leather basketball.",
            totalQuantity = 10,
            maxPerStudent = 2,
            maxDurationHours = 4,
            isActive = true
        )
        val eq2 = Equipment(
            equipmentId = "eq_2",
            name = "Volleyball (Mikasa V200W)",
            category = "Balls",
            description = "Official competition 18-panel aerodynamically designed volleyball.",
            totalQuantity = 8,
            maxPerStudent = 2,
            maxDurationHours = 4,
            isActive = true
        )
        val eq3 = Equipment(
            equipmentId = "eq_3",
            name = "Badminton Racket (Yonex Astrox)",
            category = "Rackets",
            description = "Lightweight graphite badminton racket suitable for practice.",
            totalQuantity = 12,
            maxPerStudent = 2,
            maxDurationHours = 3,
            isActive = true
        )
        val eq4 = Equipment(
            equipmentId = "eq_4",
            name = "Table Tennis Paddle (Butterfly)",
            category = "Rackets",
            description = "Standard table tennis paddle with ITTF-approved rubber.",
            totalQuantity = 6,
            maxPerStudent = 2,
            maxDurationHours = 2,
            isActive = true
        )
        val eq5 = Equipment(
            equipmentId = "eq_5",
            name = "Tournament Chess Set",
            category = "Board Games",
            description = "Weighted plastic chess pieces with foldable vinyl board.",
            totalQuantity = 5,
            maxPerStudent = 1,
            maxDurationHours = 4,
            isActive = true
        )
        val eq6 = Equipment(
            equipmentId = "eq_6",
            name = "Fencing Protective Mask",
            category = "Protective Gear",
            description = "CE 350N rated mesh fencing mask for PE practice.",
            totalQuantity = 4,
            maxPerStudent = 1,
            maxDurationHours = 3,
            isActive = true
        )
        equipmentList.addAll(listOf(eq1, eq2, eq3, eq4, eq5, eq6))

        // Timestamps for Mock Reservations
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L

        // 1. Upcoming RESERVED reservation for Student 1
        val res1 = Reservation(
            reservationId = "res_1",
            userId = student1.userId,
            studentName = student1.fullName,
            equipmentId = eq1.equipmentId,
            equipmentName = eq1.name,
            quantity = 2,
            startAt = now + 2 * hourMs,
            endAt = now + 5 * hourMs,
            status = ReservationStatus.RESERVED,
            createdAt = now - hourMs
        )

        // 2. Active ISSUED reservation for Student 1
        val res2 = Reservation(
            reservationId = "res_2",
            userId = student1.userId,
            studentName = student1.fullName,
            equipmentId = eq3.equipmentId,
            equipmentName = eq3.name,
            quantity = 1,
            startAt = now - hourMs,
            endAt = now + 2 * hourMs,
            status = ReservationStatus.ISSUED,
            createdAt = now - 3 * hourMs,
            issuedAt = now - hourMs
        )

        // 3. OVERDUE ISSUED reservation for Student 2
        val res3 = Reservation(
            reservationId = "res_3",
            userId = student2.userId,
            studentName = student2.fullName,
            equipmentId = eq2.equipmentId,
            equipmentName = eq2.name,
            quantity = 2,
            startAt = now - 5 * hourMs,
            endAt = now - 2 * hourMs,
            status = ReservationStatus.ISSUED,
            createdAt = now - 6 * hourMs,
            issuedAt = now - 5 * hourMs
        )

        // 4. Completed RETURNED reservation
        val res4 = Reservation(
            reservationId = "res_4",
            userId = student1.userId,
            studentName = student1.fullName,
            equipmentId = eq4.equipmentId,
            equipmentName = eq4.name,
            quantity = 1,
            startAt = now - 24 * hourMs,
            endAt = now - 22 * hourMs,
            status = ReservationStatus.RETURNED,
            createdAt = now - 25 * hourMs,
            issuedAt = now - 24 * hourMs,
            returnedAt = now - 22 * hourMs
        )

        // 5. CANCELLED reservation
        val res5 = Reservation(
            reservationId = "res_5",
            userId = student2.userId,
            studentName = student2.fullName,
            equipmentId = eq5.equipmentId,
            equipmentName = eq5.name,
            quantity = 1,
            startAt = now + 24 * hourMs,
            endAt = now + 26 * hourMs,
            status = ReservationStatus.CANCELLED,
            createdAt = now - 2 * hourMs,
            cancelReason = "Student schedule conflict"
        )

        reservations.addAll(listOf(res1, res2, res3, res4, res5))
    }

    private fun syncWithFirebase() {
        // Upload initial users, equipment, and reservations to Firebase Firestore
        users.forEach { user ->
            FirebaseManager.saveUserToFirebase(user)
        }
        equipmentList.forEach { eq ->
            FirebaseManager.saveEquipmentToFirebase(eq)
        }
        reservations.forEach { res ->
            FirebaseManager.saveReservationToFirebase(res)
        }

        // Attach Real-Time Listeners for Firebase Firestore READ
        FirebaseManager.listenToUsers { remoteUsers ->
            if (remoteUsers.isNotEmpty()) {
                remoteUsers.forEach { remoteUser ->
                    val idx = users.indexOfFirst { it.userId == remoteUser.userId }
                    if (idx != -1) {
                        users[idx] = remoteUser
                    } else {
                        users.add(remoteUser)
                    }
                }
            }
        }

        FirebaseManager.listenToEquipment { remoteList ->
            if (remoteList.isNotEmpty()) {
                remoteList.forEach { remoteEq ->
                    val idx = equipmentList.indexOfFirst { it.equipmentId == remoteEq.equipmentId }
                    if (idx != -1) {
                        equipmentList[idx] = remoteEq
                    } else {
                        equipmentList.add(remoteEq)
                    }
                }
            }
        }

        FirebaseManager.listenToReservations { remoteReservations ->
            if (remoteReservations.isNotEmpty()) {
                remoteReservations.forEach { remoteRes ->
                    val idx = reservations.indexOfFirst { it.reservationId == remoteRes.reservationId }
                    if (idx != -1) {
                        reservations[idx] = remoteRes
                    } else {
                        reservations.add(0, remoteRes)
                    }
                }
            }
        }
    }

    // --- Users ---
    fun getUsers(): List<User> = users.toList()

    fun getUserByEmail(email: String): User? {
        return users.find { it.email.equals(email, ignoreCase = true) }
    }

    fun registerStudent(
        prefix: String,
        firstName: String,
        middleName: String,
        lastName: String,
        email: String,
        studentId: String
    ): Result<User> {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || studentId.isBlank()) {
            return Result.failure(IllegalArgumentException("First name, last name, email, and student ID are required."))
        }
        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        val newUser = User(
            userId = "std_${UUID.randomUUID().toString().take(6)}",
            prefix = prefix.trim(),
            firstName = firstName.trim(),
            middleName = middleName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            studentId = studentId.trim(),
            role = UserRole.STUDENT
        )
        users.add(newUser)

        // Firebase Firestore CREATE: Persist new user profile to "users" collection
        FirebaseManager.saveUserToFirebase(newUser)

        return Result.success(newUser)
    }

    // --- Equipment ---
    fun getAllEquipment(includeInactive: Boolean = false): List<Equipment> {
        return if (includeInactive) equipmentList.toList() else equipmentList.filter { it.isActive }
    }

    fun getEquipmentById(equipmentId: String): Equipment? {
        return equipmentList.find { it.equipmentId == equipmentId }
    }

    fun getCategories(): List<String> {
        val categories = equipmentList.map { it.category }.distinct().sorted()
        return listOf("All") + categories
    }

    fun calculateAvailableQuantity(equipmentId: String, startAt: Long, endAt: Long, excludeReservationId: String? = null): Int {
        val eq = getEquipmentById(equipmentId) ?: return 0
        if (!eq.isActive) return 0

        val overlappingReservations = reservations.filter { res ->
            res.equipmentId == equipmentId &&
                    res.reservationId != excludeReservationId &&
                    (res.status == ReservationStatus.RESERVED || res.status == ReservationStatus.ISSUED) &&
                    (startAt < res.endAt && endAt > res.startAt)
        }

        val reservedQuantity = overlappingReservations.sumOf { it.quantity }
        val available = eq.totalQuantity - reservedQuantity
        return if (available < 0) 0 else available
    }

    // --- Reservations ---
    fun getReservationsForUser(userId: String): List<Reservation> {
        return reservations.filter { it.userId == userId }.sortedByDescending { it.createdAt }
    }

    fun getAllReservations(): List<Reservation> {
        return reservations.sortedByDescending { it.createdAt }
    }

    fun createReservation(
        userId: String,
        studentName: String,
        equipmentId: String,
        quantity: Int,
        startAt: Long,
        endAt: Long
    ): Result<Reservation> {
        val eq = getEquipmentById(equipmentId)
            ?: return Result.failure(IllegalArgumentException("Equipment not found."))

        if (!eq.isActive) {
            return Result.failure(IllegalStateException("Equipment is deactivated and unavailable for reservation."))
        }

        val now = System.currentTimeMillis() - 60_000
        if (startAt < now) {
            return Result.failure(IllegalArgumentException("Reservation start time must be in the future."))
        }

        if (endAt <= startAt) {
            return Result.failure(IllegalArgumentException("End time must be later than start time."))
        }

        val maxDurationMs = eq.maxDurationHours * 3600_000L
        if ((endAt - startAt) > maxDurationMs) {
            return Result.failure(IllegalArgumentException("Reservation duration exceeds maximum allowed (${eq.maxDurationHours} hours)."))
        }

        if (quantity < 1) {
            return Result.failure(IllegalArgumentException("Quantity must be at least 1."))
        }

        if (quantity > eq.maxPerStudent) {
            return Result.failure(IllegalArgumentException("Requested quantity exceeds maximum limit per student (${eq.maxPerStudent} units)."))
        }

        val availableStock = calculateAvailableQuantity(equipmentId, startAt, endAt)
        if (quantity > availableStock) {
            return Result.failure(IllegalStateException("Insufficient stock available for the requested time window. Available units: $availableStock."))
        }

        val newReservation = Reservation(
            reservationId = "res_${UUID.randomUUID().toString().take(6)}",
            userId = userId,
            studentName = studentName,
            equipmentId = equipmentId,
            equipmentName = eq.name,
            quantity = quantity,
            startAt = startAt,
            endAt = endAt,
            status = ReservationStatus.RESERVED,
            createdAt = System.currentTimeMillis()
        )

        reservations.add(0, newReservation)
        FirebaseManager.saveReservationToFirebase(newReservation)
        return Result.success(newReservation)
    }

    fun cancelReservationByStudent(reservationId: String, userId: String): Result<Unit> {
        val index = reservations.indexOfFirst { it.reservationId == reservationId }
        if (index == -1) return Result.failure(IllegalArgumentException("Reservation not found."))

        val res = reservations[index]
        if (res.userId != userId) {
            return Result.failure(SecurityException("You can only cancel your own reservations."))
        }

        if (res.status != ReservationStatus.RESERVED) {
            return Result.failure(IllegalStateException("Only RESERVED bookings can be cancelled."))
        }

        if (res.startAt <= System.currentTimeMillis()) {
            return Result.failure(IllegalStateException("Cannot cancel a reservation that has already reached or passed its start time."))
        }

        val updated = res.copy(
            status = ReservationStatus.CANCELLED,
            cancelReason = "Cancelled by student before start time"
        )
        reservations[index] = updated
        FirebaseManager.saveReservationToFirebase(updated)
        return Result.success(Unit)
    }

    fun issueReservation(reservationId: String): Result<Unit> {
        val index = reservations.indexOfFirst { it.reservationId == reservationId }
        if (index == -1) return Result.failure(IllegalArgumentException("Reservation not found."))

        val res = reservations[index]
        if (res.status != ReservationStatus.RESERVED) {
            return Result.failure(IllegalStateException("Only RESERVED bookings can be marked as ISSUED."))
        }

        val updated = res.copy(
            status = ReservationStatus.ISSUED,
            issuedAt = System.currentTimeMillis()
        )
        reservations[index] = updated
        FirebaseManager.saveReservationToFirebase(updated)
        return Result.success(Unit)
    }

    fun returnReservation(reservationId: String): Result<Unit> {
        val index = reservations.indexOfFirst { it.reservationId == reservationId }
        if (index == -1) return Result.failure(IllegalArgumentException("Reservation not found."))

        val res = reservations[index]
        if (res.status != ReservationStatus.ISSUED) {
            return Result.failure(IllegalStateException("Only ISSUED equipment can be marked as RETURNED."))
        }

        val updated = res.copy(
            status = ReservationStatus.RETURNED,
            returnedAt = System.currentTimeMillis()
        )
        reservations[index] = updated
        FirebaseManager.saveReservationToFirebase(updated)
        return Result.success(Unit)
    }

    fun cancelReservationByStaff(reservationId: String, reason: String): Result<Unit> {
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("A cancellation reason must be provided."))
        }
        val index = reservations.indexOfFirst { it.reservationId == reservationId }
        if (index == -1) return Result.failure(IllegalArgumentException("Reservation not found."))

        val res = reservations[index]
        if (res.status != ReservationStatus.RESERVED) {
            return Result.failure(IllegalStateException("Only RESERVED bookings can be cancelled."))
        }

        val updated = res.copy(
            status = ReservationStatus.CANCELLED,
            cancelReason = reason.trim()
        )
        reservations[index] = updated
        FirebaseManager.saveReservationToFirebase(updated)
        return Result.success(Unit)
    }

    fun deleteReservation(reservationId: String): Result<Unit> {
        val index = reservations.indexOfFirst { it.reservationId == reservationId }
        if (index == -1) return Result.failure(IllegalArgumentException("Reservation not found."))

        val res = reservations[index]
        if (res.status != ReservationStatus.RETURNED && res.status != ReservationStatus.CANCELLED) {
            return Result.failure(IllegalStateException("Only CANCELLED or RETURNED activity records can be deleted."))
        }

        reservations.removeAt(index)
        FirebaseManager.deleteReservationFromFirebase(reservationId)
        return Result.success(Unit)
    }

    // --- Manage Equipment (Staff) ---
    fun saveEquipment(
        equipmentId: String?,
        name: String,
        category: String,
        description: String,
        totalQuantity: Int,
        maxPerStudent: Int,
        maxDurationHours: Int
    ): Result<Equipment> {
        if (name.isBlank() || category.isBlank()) {
            return Result.failure(IllegalArgumentException("Name and category are required."))
        }
        if (totalQuantity <= 0 || maxPerStudent <= 0 || maxDurationHours <= 0) {
            return Result.failure(IllegalArgumentException("Quantities and duration limits must be greater than zero."))
        }

        val updatedEq = if (equipmentId.isNullOrBlank()) {
            Equipment(
                equipmentId = "eq_${UUID.randomUUID().toString().take(6)}",
                name = name.trim(),
                category = category.trim(),
                description = description.trim(),
                totalQuantity = totalQuantity,
                maxPerStudent = maxPerStudent,
                maxDurationHours = maxDurationHours,
                isActive = true
            ).also { equipmentList.add(it) }
        } else {
            val index = equipmentList.indexOfFirst { it.equipmentId == equipmentId }
            if (index == -1) return Result.failure(IllegalArgumentException("Equipment not found."))
            equipmentList[index].copy(
                name = name.trim(),
                category = category.trim(),
                description = description.trim(),
                totalQuantity = totalQuantity,
                maxPerStudent = maxPerStudent,
                maxDurationHours = maxDurationHours
            ).also { equipmentList[index] = it }
        }

        FirebaseManager.saveEquipmentToFirebase(updatedEq)
        return Result.success(updatedEq)
    }

    fun toggleEquipmentActive(equipmentId: String): Result<Equipment> {
        val index = equipmentList.indexOfFirst { it.equipmentId == equipmentId }
        if (index == -1) return Result.failure(IllegalArgumentException("Equipment not found."))

        val eq = equipmentList[index]
        val updated = eq.copy(isActive = !eq.isActive)
        equipmentList[index] = updated

        FirebaseManager.saveEquipmentToFirebase(updated)
        return Result.success(updated)
    }

    fun deleteEquipment(equipmentId: String): Result<Unit> {
        val isReferenced = reservations.any { it.equipmentId == equipmentId }
        if (isReferenced) {
            return Result.failure(IllegalStateException("Cannot delete equipment because existing reservation records refer to it. Deactivate the equipment instead."))
        }
        equipmentList.removeIf { it.equipmentId == equipmentId }
        return Result.success(Unit)
    }
}
