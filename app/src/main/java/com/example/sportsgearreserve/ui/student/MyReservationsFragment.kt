package com.example.sportsgearreserve.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportsgearreserve.data.SessionManager
import com.example.sportsgearreserve.data.SportsRepository
import com.example.sportsgearreserve.databinding.FragmentMyReservationsBinding
import com.example.sportsgearreserve.models.Reservation
import com.example.sportsgearreserve.models.ReservationStatus
import com.example.sportsgearreserve.ui.adapters.ReservationAdapter

class MyReservationsFragment : Fragment() {

    private var _binding: FragmentMyReservationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: ReservationAdapter
    private var allReservations = listOf<Reservation>()
    private var currentFilterId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReservationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        currentFilterId = binding.chipAll.id

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
            isStaffMode = false,
            onCancelClick = { reservation ->
                handleStudentCancel(reservation)
            }
        )
        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter
    }

    private fun setupChipListeners() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterId = checkedIds.firstOrNull() ?: binding.chipAll.id
            filterReservations()
        }
    }

    private fun loadData() {
        val user = sessionManager.getUser() ?: return
        allReservations = SportsRepository.getReservationsForUser(user.userId)
        filterReservations()
    }

    private fun filterReservations() {
        val filtered = when (currentFilterId) {
            binding.chipUpcoming.id -> allReservations.filter { it.status == ReservationStatus.RESERVED }
            binding.chipIssued.id -> allReservations.filter { it.status == ReservationStatus.ISSUED }
            binding.chipHistory.id -> allReservations.filter { it.status == ReservationStatus.RETURNED || it.status == ReservationStatus.CANCELLED }
            else -> allReservations
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvReservations.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvReservations.visibility = View.VISIBLE
        }
    }

    private fun handleStudentCancel(reservation: Reservation) {
        val user = sessionManager.getUser() ?: return
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Reservation?")
            .setMessage("Are you sure you want to cancel your reservation for ${reservation.equipmentName}? This will free up the stock for other students.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                val result = SportsRepository.cancelReservationByStudent(reservation.reservationId, user.userId)
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Reservation cancelled.", Toast.LENGTH_SHORT).show()
                        loadData()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error.message ?: "Cancellation failed.", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Keep Reservation", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
