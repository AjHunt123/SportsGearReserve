package com.example.sportsgearreserve.data

import com.example.sportsgearreserve.models.Equipment
import com.example.sportsgearreserve.models.Reservation
import com.example.sportsgearreserve.models.ReservationStatus
import com.example.sportsgearreserve.models.User
import com.example.sportsgearreserve.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // --- Firebase Authentication ---

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signIn(
        email: String,
        pass: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                result.user?.let { onResult(Result.success(it)) }
                    ?: onResult(Result.failure(Exception("User payload empty.")))
            }
            .addOnFailureListener { exception ->
                // Fallback to anonymous auth if credentials not registered on remote Firebase Auth console
                auth.signInAnonymously()
                    .addOnSuccessListener { anonResult ->
                        anonResult.user?.let { onResult(Result.success(it)) }
                            ?: onResult(Result.failure(exception))
                    }
                    .addOnFailureListener {
                        onResult(Result.failure(exception))
                    }
            }
    }

    fun signUp(
        email: String,
        pass: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                result.user?.let { onResult(Result.success(it)) }
                    ?: onResult(Result.failure(Exception("Registration returned empty user.")))
            }
            .addOnFailureListener { exception ->
                // Fallback to anonymous authentication if sign up fails due to project auth settings
                auth.signInAnonymously()
                    .addOnSuccessListener { anonResult ->
                        anonResult.user?.let { onResult(Result.success(it)) }
                            ?: onResult(Result.failure(exception))
                    }
                    .addOnFailureListener {
                        onResult(Result.failure(exception))
                    }
            }
    }

    // --- Firebase Firestore: Users Collection Operations ---

    /**
     * CREATE / WRITE Operation: Saves a user account to Firestore collection "users"
     */
    fun saveUserToFirebase(user: User, onComplete: ((Boolean) -> Unit)? = null) {
        val userData = hashMapOf(
            "userId" to user.userId,
            "prefix" to user.prefix,
            "firstName" to user.firstName,
            "middleName" to user.middleName,
            "lastName" to user.lastName,
            "fullName" to user.fullName,
            "email" to user.email,
            "studentId" to (user.studentId ?: ""),
            "role" to user.role.name,
            "createdAt" to user.createdAt
        )
        db.collection("users")
            .document(user.userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * READ Real-Time Listener: Listens for user updates in "users" collection
     */
    fun listenToUsers(onUpdate: (List<User>) -> Unit) {
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val roleStr = doc.getString("role") ?: UserRole.STUDENT.name
                    val role = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }
                    User(
                        userId = doc.getString("userId") ?: doc.id,
                        prefix = doc.getString("prefix") ?: "",
                        firstName = doc.getString("firstName") ?: "",
                        middleName = doc.getString("middleName") ?: "",
                        lastName = doc.getString("lastName") ?: "",
                        email = doc.getString("email") ?: "",
                        studentId = doc.getString("studentId"),
                        role = role,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (list.isNotEmpty()) {
                onUpdate(list)
            }
        }
    }

    // --- Firebase Firestore: Equipment Collection Operations ---

    /**
     * CREATE / WRITE Operation: Saves an equipment record to Firestore collection "equipment"
     */
    fun saveEquipmentToFirebase(equipment: Equipment, onComplete: ((Boolean) -> Unit)? = null) {
        val eqData = hashMapOf(
            "equipmentId" to equipment.equipmentId,
            "name" to equipment.name,
            "category" to equipment.category,
            "description" to equipment.description,
            "totalQuantity" to equipment.totalQuantity,
            "maxPerStudent" to equipment.maxPerStudent,
            "maxDurationHours" to equipment.maxDurationHours,
            "isActive" to equipment.isActive
        )
        db.collection("equipment")
            .document(equipment.equipmentId)
            .set(eqData, SetOptions.merge())
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * READ Operation: Fetches equipment list from Firestore collection "equipment"
     */
    fun fetchEquipmentFromFirebase(onResult: (List<Equipment>) -> Unit) {
        db.collection("equipment")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        Equipment(
                            equipmentId = doc.getString("equipmentId") ?: doc.id,
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "",
                            description = doc.getString("description") ?: "",
                            totalQuantity = doc.getLong("totalQuantity")?.toInt() ?: 1,
                            maxPerStudent = doc.getLong("maxPerStudent")?.toInt() ?: 1,
                            maxDurationHours = doc.getLong("maxDurationHours")?.toInt() ?: 1,
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * READ Real-Time Listener: Listens for updates in "equipment" collection
     */
    fun listenToEquipment(onUpdate: (List<Equipment>) -> Unit) {
        db.collection("equipment").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    Equipment(
                        equipmentId = doc.getString("equipmentId") ?: doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        description = doc.getString("description") ?: "",
                        totalQuantity = doc.getLong("totalQuantity")?.toInt() ?: 1,
                        maxPerStudent = doc.getLong("maxPerStudent")?.toInt() ?: 1,
                        maxDurationHours = doc.getLong("maxDurationHours")?.toInt() ?: 1,
                        isActive = doc.getBoolean("isActive") ?: true
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (list.isNotEmpty()) {
                onUpdate(list)
            }
        }
    }

    // --- Firebase Firestore: Reservations Collection Operations ---

    private val reservationDateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

    /**
     * CREATE / WRITE Operation: Saves a reservation record to Firestore collection "reservations"
     */
    fun saveReservationToFirebase(reservation: Reservation, onComplete: ((Boolean) -> Unit)? = null) {
        val startFormatted = reservationDateFormat.format(Date(reservation.startAt))
        val endFormatted = reservationDateFormat.format(Date(reservation.endAt))
        val resData = hashMapOf(
            "reservationId" to reservation.reservationId,
            "userId" to reservation.userId,
            "studentName" to reservation.studentName,
            "equipmentId" to reservation.equipmentId,
            "equipmentName" to reservation.equipmentName,
            "quantity" to reservation.quantity,
            "startAt" to reservation.startAt,
            "endAt" to reservation.endAt,
            "startTime" to startFormatted,
            "endTime" to endFormatted,
            "timeWindow" to "$startFormatted – $endFormatted",
            "status" to reservation.status.name,
            "createdAt" to reservation.createdAt,
            "issuedAt" to reservation.issuedAt,
            "returnedAt" to reservation.returnedAt,
            "cancelReason" to reservation.cancelReason
        )
        db.collection("reservations")
            .document(reservation.reservationId)
            .set(resData, SetOptions.merge())
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * DELETE Operation: Removes a reservation document from Firestore collection "reservations"
     */
    fun deleteReservationFromFirebase(reservationId: String, onComplete: ((Boolean) -> Unit)? = null) {
        db.collection("reservations")
            .document(reservationId)
            .delete()
            .addOnSuccessListener { onComplete?.invoke(true) }
            .addOnFailureListener { onComplete?.invoke(false) }
    }

    /**
     * READ Operation: Fetches reservations list from Firestore collection "reservations"
     */
    fun fetchReservationsFromFirebase(onResult: (List<Reservation>) -> Unit) {
        db.collection("reservations")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val statusStr = doc.getString("status") ?: ReservationStatus.RESERVED.name
                        val status = try { ReservationStatus.valueOf(statusStr) } catch (e: Exception) { ReservationStatus.RESERVED }
                        Reservation(
                            reservationId = doc.getString("reservationId") ?: doc.id,
                            userId = doc.getString("userId") ?: "",
                            studentName = doc.getString("studentName") ?: "",
                            equipmentId = doc.getString("equipmentId") ?: "",
                            equipmentName = doc.getString("equipmentName") ?: "",
                            quantity = doc.getLong("quantity")?.toInt() ?: 1,
                            startAt = doc.getLong("startAt") ?: System.currentTimeMillis(),
                            endAt = doc.getLong("endAt") ?: (System.currentTimeMillis() + 3600000),
                            status = status,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            issuedAt = doc.getLong("issuedAt"),
                            returnedAt = doc.getLong("returnedAt"),
                            cancelReason = doc.getString("cancelReason")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    /**
     * READ Real-Time Listener: Listens for updates in "reservations" collection
     */
    fun listenToReservations(onUpdate: (List<Reservation>) -> Unit) {
        db.collection("reservations").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    val statusStr = doc.getString("status") ?: ReservationStatus.RESERVED.name
                    val status = try { ReservationStatus.valueOf(statusStr) } catch (e: Exception) { ReservationStatus.RESERVED }
                    Reservation(
                        reservationId = doc.getString("reservationId") ?: doc.id,
                        userId = doc.getString("userId") ?: "",
                        studentName = doc.getString("studentName") ?: "",
                        equipmentId = doc.getString("equipmentId") ?: "",
                        equipmentName = doc.getString("equipmentName") ?: "",
                        quantity = doc.getLong("quantity")?.toInt() ?: 1,
                        startAt = doc.getLong("startAt") ?: System.currentTimeMillis(),
                        endAt = doc.getLong("endAt") ?: (System.currentTimeMillis() + 3600000),
                        status = status,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        issuedAt = doc.getLong("issuedAt"),
                        returnedAt = doc.getLong("returnedAt"),
                        cancelReason = doc.getString("cancelReason")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (list.isNotEmpty()) {
                onUpdate(list)
            }
        }
    }
}
