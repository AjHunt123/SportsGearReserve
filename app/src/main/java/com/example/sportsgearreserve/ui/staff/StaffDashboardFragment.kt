package com.example.sportsgearreserve.ui.staff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportsgearreserve.data.SportsRepository
import com.example.sportsgearreserve.databinding.DialogCancelReasonBinding
import com.example.sportsgearreserve.databinding.FragmentStaffDashboardBinding
import com.example.sportsgearreserve.models.Reservation
import com.example.sportsgearreserve.models.ReservationStatus
import com.example.sportsgearreserve.ui.adapters.ReservationAdapter

class StaffDashboardFragment : Fragment() {

    private var _binding: FragmentStaffDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReservationAdapter
    private var allReservations = listOf<Reservation>()
    private var currentFilterId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentFilterId = binding.chipStaffAll.id

        setupRecyclerView()
        setupChipListeners()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = ReservationAdapter(
            reservationList = emptyList(),
            isStaffMode = true,
            onIssueClick = { reservation -> handleIssue(reservation) },
            onReturnClick = { reservation -> handleReturn(reservation) },
            onCancelClick = { reservation -> handleCancelWithReason(reservation) }
        )
        binding.rvStaffQueue.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStaffQueue.adapter = adapter
    }

    private fun setupChipListeners() {
        binding.chipGroupStaffFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterId = checkedIds.firstOrNull() ?: binding.chipStaffAll.id
            filterReservations()
        }
    }

    private fun loadData() {
        allReservations = SportsRepository.getAllReservations()

        // Check for overdue items (BR-05, FR-11)
        val overdueCount = allReservations.count { it.isOverdue }
        if (overdueCount > 0) {
            binding.cardOverdueAlert.visibility = View.VISIBLE
            binding.tvOverdueAlertText.text = "$overdueCount item(s) currently OVERDUE for return!"
        } else {
            binding.cardOverdueAlert.visibility = View.GONE
        }

        filterReservations()
    }

    private fun filterReservations() {
        val filtered = when (currentFilterId) {
            binding.chipStaffOverdue.id -> allReservations.filter { it.isOverdue }
            binding.chipStaffReserved.id -> allReservations.filter { it.status == ReservationStatus.RESERVED }
            binding.chipStaffIssued.id -> allReservations.filter { it.status == ReservationStatus.ISSUED }
            binding.chipStaffHistory.id -> allReservations.filter { it.status == ReservationStatus.RETURNED || it.status == ReservationStatus.CANCELLED }
            else -> allReservations
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvStaffQueue.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvStaffQueue.visibility = View.VISIBLE
        }
    }

    private fun handleIssue(reservation: Reservation) {
        AlertDialog.Builder(requireContext())
            .setTitle("Issue Equipment?")
            .setMessage("Confirm issuing ${reservation.quantity} unit(s) of ${reservation.equipmentName} to student ${reservation.studentName}?")
            .setPositiveButton("Confirm Issue") { _, _ ->
                val result = SportsRepository.issueReservation(reservation.reservationId)
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Equipment marked as ISSUED.", Toast.LENGTH_SHORT).show()
                        loadData()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error.message ?: "Failed to issue.", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleReturn(reservation: Reservation) {
        AlertDialog.Builder(requireContext())
            .setTitle("Receive Return?")
            .setMessage("Confirm equipment return for ${reservation.equipmentName} from ${reservation.studentName}?")
            .setPositiveButton("Confirm Return") { _, _ ->
                val result = SportsRepository.returnReservation(reservation.reservationId)
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Equipment marked as RETURNED. Inventory restored.", Toast.LENGTH_SHORT).show()
                        loadData()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error.message ?: "Failed to record return.", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleCancelWithReason(reservation: Reservation) {
        val dialogBinding = DialogCancelReasonBinding.inflate(layoutInflater)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Submit Cancellation") { _, _ ->
                val reason = dialogBinding.etCancelReason.text.toString().trim()
                if (reason.isBlank()) {
                    Toast.makeText(requireContext(), "Cancellation reason is required.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val result = SportsRepository.cancelReservationByStaff(reservation.reservationId, reason)
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Reservation cancelled with reason.", Toast.LENGTH_SHORT).show()
                        loadData()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error.message ?: "Cancellation failed.", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Back", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
