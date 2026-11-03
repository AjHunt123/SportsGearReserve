package com.example.sportsgearreserve

import com.example.sportsgearreserve.data.SportsRepository
import com.example.sportsgearreserve.models.ReservationStatus
import com.example.sportsgearreserve.models.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SportsRepositoryTest {

    @Before
    fun setUp() {
        // Repository initializes seed mock data
    }

    @Test
    fun testSeedDataLoaded() {
        val users = SportsRepository.getUsers()
        assertTrue("Users should be populated", users.isNotEmpty())

        val equipment = SportsRepository.getAllEquipment(includeInactive = true)
        assertTrue("Equipment should be populated", equipment.isNotEmpty())

        val reservations = SportsRepository.getAllReservations()
        assertTrue("Reservations should be populated", reservations.isNotEmpty())
    }

    @Test
    fun testOverlappingAvailabilityCalculation() {
        val eq = SportsRepository.getEquipmentById("eq_1")!! // Basketball, total 10 units
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L

        // In seed data, res_1 is for eq_1 with quantity 2 from (now + 2h) to (now + 5h)
        // Testing an overlapping window from (now + 3h) to (now + 4h)
        val availableDuringOverlap = SportsRepository.calculateAvailableQuantity(
            equipmentId = eq.equipmentId,
            startAt = now + 3 * hourMs,
            endAt = now + 4 * hourMs
        )
        assertEquals("Should deduct 2 reserved units during overlap window", eq.totalQuantity - 2, availableDuringOverlap)

        // Testing a non-overlapping window from (now + 10h) to (now + 12h)
        val availableNonOverlap = SportsRepository.calculateAvailableQuantity(
            equipmentId = eq.equipmentId,
            startAt = now + 10 * hourMs,
            endAt = now + 12 * hourMs
        )
        assertEquals("Should return full total quantity for non-overlapping window", eq.totalQuantity, availableNonOverlap)
    }

    @Test
    fun testCreateReservationSuccess() {
        val student = SportsRepository.getUsers().first { it.role == UserRole.STUDENT }
        val eq = SportsRepository.getEquipmentById("eq_3")!! // Badminton Racket, max 2 per student, max 3h
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L

        val result = SportsRepository.createReservation(
            userId = student.userId,
            studentName = student.fullName,
            equipmentId = eq.equipmentId,
            quantity = 2,
            startAt = now + 20 * hourMs,
            endAt = now + 22 * hourMs
        )

        assertTrue("Reservation should succeed", result.isSuccess)
        val res = result.getOrNull()
        assertNotNull(res)
        assertEquals(ReservationStatus.RESERVED, res?.status)
        assertEquals(2, res?.quantity)
    }

    @Test
    fun testCreateReservationFailsOnQuantityExceedingMaxPerStudent() {
        val student = SportsRepository.getUsers().first { it.role == UserRole.STUDENT }
        val eq = SportsRepository.getEquipmentById("eq_1")!! // Basketball, maxPerStudent = 2
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L

        val result = SportsRepository.createReservation(
            userId = student.userId,
            studentName = student.fullName,
            equipmentId = eq.equipmentId,
            quantity = 5, // Exceeds maxPerStudent = 2
            startAt = now + 20 * hourMs,
            endAt = now + 22 * hourMs
        )

        assertTrue("Reservation should fail when exceeding maxPerStudent", result.isFailure)
    }

    @Test
    fun testCreateReservationFailsOnExceedingMaxDuration() {
        val student = SportsRepository.getUsers().first { it.role == UserRole.STUDENT }
        val eq = SportsRepository.getEquipmentById("eq_4")!! // Table Tennis Paddle, maxDurationHours = 2
        val now = System.currentTimeMillis()
        val hourMs = 3600_000L

        val result = SportsRepository.createReservation(
            userId = student.userId,
            studentName = student.fullName,
            equipmentId = eq.equipmentId,
            quantity = 1,
            startAt = now + 20 * hourMs,
            endAt = now + 25 * hourMs // 5 hours exceeds max 2h
        )

        assertTrue("Reservation should fail when duration exceeds max limit", result.isFailure)
    }

    @Test
    fun testStatusTransitionsAndOverdueDetection() {
        val res = SportsRepository.getAllReservations().first { it.reservationId == "res_3" }
        // res_3 in seed data is ISSUED and endAt is in the past
        assertTrue("res_3 should be detected as overdue", res.isOverdue)
        assertEquals("OVERDUE", res.displayStatus)
    }

    @Test
    fun testDeleteEquipmentRestrictedWhenReferencedByReservation() {
        // eq_1 has reservations
        val deleteResult = SportsRepository.deleteEquipment("eq_1")
        assertTrue("Deleting equipment referenced by reservations must fail", deleteResult.isFailure)
    }
}
