package com.example.sportsgearreserve.ui.student

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sportsgearreserve.R
import com.example.sportsgearreserve.data.SessionManager
import com.example.sportsgearreserve.data.SportsRepository
import com.example.sportsgearreserve.databinding.ActivityEquipmentDetailBinding
import com.example.sportsgearreserve.models.Equipment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EquipmentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquipmentDetailBinding
    private lateinit var sessionManager: SessionManager
    private var equipment: Equipment? = null

    private val selectedCalendar = Calendar.getInstance()
    private var startHour = 14
    private var startMinute = 0
    private var endHour = 16
    private var endMinute = 0

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipmentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val equipmentId = intent.getStringExtra("EQUIPMENT_ID")
        if (equipmentId.isNullOrBlank()) {
            Toast.makeText(this, "Invalid equipment selected.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        equipment = SportsRepository.getEquipmentById(equipmentId)
        if (equipment == null) {
            Toast.makeText(this, "Equipment not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        populateEquipmentInfo()
        setupDateTimeDefaults()
        setupPickers()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun populateEquipmentInfo() {
        val eq = equipment ?: return
        binding.tvEqName.text = eq.name
        binding.tvEqCategory.text = "Category: ${eq.category}"
        binding.tvEqDescription.text = eq.description
        binding.tvTotalStock.text = "Total Stock: ${eq.totalQuantity} units"
        binding.tvLimits.text = "Max ${eq.maxPerStudent}/student | Max ${eq.maxDurationHours}h"
    }

    private fun setupDateTimeDefaults() {
        // Set default date to today or tomorrow if late
        selectedCalendar.add(Calendar.HOUR_OF_DAY, 1)
        startHour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
        startMinute = 0
        endHour = (startHour + 2) % 24
        endMinute = 0

        binding.etDate.setText(dateFormatter.format(selectedCalendar.time))
        binding.etStartTime.setText(String.format(Locale.getDefault(), "%02d:00", startHour))
        binding.etEndTime.setText(String.format(Locale.getDefault(), "%02d:00", endHour))
    }

    private fun setupPickers() {
        binding.etDate.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.etDate.setText(dateFormatter.format(selectedCalendar.time))
            }
            DatePickerDialog(
                this,
                dateSetListener,
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        binding.etStartTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                startHour = hourOfDay
                startMinute = minute
                binding.etStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            }, startHour, startMinute, true).show()
        }

        binding.etEndTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                endHour = hourOfDay
                endMinute = minute
                binding.etEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            }, endHour, endMinute, true).show()
        }
    }

    private fun setupButtons() {
        binding.btnCheckAvailability.setOnClickListener {
            calculateAndDisplayAvailability()
        }

        binding.btnSubmitReservation.setOnClickListener {
            handleReservationSubmit()
        }
    }

    private fun getTimestamps(): Pair<Long, Long>? {
        val dateStr = binding.etDate.text.toString().trim()
        if (dateStr.isBlank()) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show()
            return null
        }

        try {
            val calStart = Calendar.getInstance()
            calStart.time = dateFormatter.parse(dateStr)!!
            calStart.set(Calendar.HOUR_OF_DAY, startHour)
            calStart.set(Calendar.MINUTE, startMinute)
            calStart.set(Calendar.SECOND, 0)
            calStart.set(Calendar.MILLISECOND, 0)

            val calEnd = Calendar.getInstance()
            calEnd.time = dateFormatter.parse(dateStr)!!
            calEnd.set(Calendar.HOUR_OF_DAY, endHour)
            calEnd.set(Calendar.MINUTE, endMinute)
            calEnd.set(Calendar.SECOND, 0)
            calEnd.set(Calendar.MILLISECOND, 0)

            val startAt = calStart.timeInMillis
            var endAt = calEnd.timeInMillis

            if (endAt <= startAt) {
                // Handle midnight crossover if end time is earlier
                calEnd.add(Calendar.DAY_OF_MONTH, 1)
                endAt = calEnd.timeInMillis
            }

            return Pair(startAt, endAt)
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date/time format.", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun calculateAndDisplayAvailability() {
        val eq = equipment ?: return
        val timestamps = getTimestamps() ?: return
        val (startAt, endAt) = timestamps

        val available = SportsRepository.calculateAvailableQuantity(eq.equipmentId, startAt, endAt)
        binding.tvAvailabilityResult.visibility = View.VISIBLE

        if (available > 0) {
            binding.tvAvailabilityResult.text = "AVAILABLE: $available of ${eq.totalQuantity} units are free for this time window."
            binding.tvAvailabilityResult.setBackgroundColor(ContextCompat.getColor(this, R.color.badge_active_bg))
            binding.tvAvailabilityResult.setTextColor(ContextCompat.getColor(this, R.color.badge_active_text))
        } else {
            binding.tvAvailabilityResult.text = "UNAVAILABLE: All ${eq.totalQuantity} units are reserved/issued during this window."
            binding.tvAvailabilityResult.setBackgroundColor(ContextCompat.getColor(this, R.color.badge_overdue_bg))
            binding.tvAvailabilityResult.setTextColor(ContextCompat.getColor(this, R.color.badge_overdue_text))
        }
    }

    private fun handleReservationSubmit() {
        val eq = equipment ?: return
        val currentUser = sessionManager.getUser()
        if (currentUser == null) {
            Toast.makeText(this, "User session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamps = getTimestamps() ?: return
        val (startAt, endAt) = timestamps

        val qtyStr = binding.etQuantity.text.toString().trim()
        val quantity = qtyStr.toIntOrNull() ?: 1

        val result = SportsRepository.createReservation(
            userId = currentUser.userId,
            studentName = currentUser.fullName,
            equipmentId = eq.equipmentId,
            quantity = quantity,
            startAt = startAt,
            endAt = endAt
        )

        result.fold(
            onSuccess = { reservation ->
                AlertDialog.Builder(this)
                    .setTitle("Reservation Confirmed! 🎉")
                    .setMessage("Your reservation (ID: ${reservation.reservationId}) for ${reservation.quantity} unit(s) of ${eq.name} has been successfully recorded.\n\nStatus: RESERVED")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            },
            onFailure = { error ->
                AlertDialog.Builder(this)
                    .setTitle("Reservation Failed")
                    .setMessage(error.message ?: "Unable to place reservation.")
                    .setPositiveButton("Close", null)
                    .show()
            }
        )
    }
}
