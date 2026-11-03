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
import com.example.sportsgearreserve.databinding.DialogAddEditEquipmentBinding
import com.example.sportsgearreserve.databinding.FragmentManageEquipmentBinding
import com.example.sportsgearreserve.models.Equipment
import com.example.sportsgearreserve.ui.adapters.EquipmentAdapter

class ManageEquipmentFragment : Fragment() {

    private var _binding: FragmentManageEquipmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EquipmentAdapter
    private var allEquipment = listOf<Equipment>()
    private var currentFilterId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageEquipmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentFilterId = binding.chipManageAll.id

        setupRecyclerView()
        setupListeners()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = EquipmentAdapter(
            equipmentList = emptyList(),
            isStaffMode = true,
            onItemClick = { equipment -> showAddEditDialog(equipment) },
            onEditClick = { equipment -> showAddEditDialog(equipment) },
            onToggleActiveClick = { equipment -> handleToggleActive(equipment) },
            onDeleteClick = { equipment -> handleDelete(equipment) }
        )
        binding.rvManageEquipment.layoutManager = LinearLayoutManager(requireContext())
        binding.rvManageEquipment.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddEquipment.setOnClickListener {
            showAddEditDialog(null)
        }

        binding.chipGroupManageFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilterId = checkedIds.firstOrNull() ?: binding.chipManageAll.id
            filterEquipment()
        }
    }

    private fun loadData() {
        allEquipment = SportsRepository.getAllEquipment(includeInactive = true)
        filterEquipment()
    }

    private fun filterEquipment() {
        val filtered = when (currentFilterId) {
            binding.chipManageActive.id -> allEquipment.filter { it.isActive }
            binding.chipManageInactive.id -> allEquipment.filter { !it.isActive }
            else -> allEquipment
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvManageEquipment.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvManageEquipment.visibility = View.VISIBLE
        }
    }

    private fun showAddEditDialog(equipment: Equipment?) {
        val dialogBinding = DialogAddEditEquipmentBinding.inflate(layoutInflater)

        if (equipment != null) {
            dialogBinding.tvDialogTitle.text = "Edit Equipment"
            dialogBinding.etEqName.setText(equipment.name)
            dialogBinding.etEqCategory.setText(equipment.category)
            dialogBinding.etEqDescription.setText(equipment.description)
            dialogBinding.etTotalQuantity.setText(equipment.totalQuantity.toString())
            dialogBinding.etMaxPerStudent.setText(equipment.maxPerStudent.toString())
            dialogBinding.etMaxDuration.setText(equipment.maxDurationHours.toString())
        } else {
            dialogBinding.tvDialogTitle.text = "Add New Equipment"
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Save Record") { _, _ ->
                val name = dialogBinding.etEqName.text.toString().trim()
                val category = dialogBinding.etEqCategory.text.toString().trim()
                val description = dialogBinding.etEqDescription.text.toString().trim()
                val totalQty = dialogBinding.etTotalQuantity.text.toString().toIntOrNull() ?: 0
                val maxPerStudent = dialogBinding.etMaxPerStudent.text.toString().toIntOrNull() ?: 2
                val maxDuration = dialogBinding.etMaxDuration.text.toString().toIntOrNull() ?: 4

                val result = SportsRepository.saveEquipment(
                    equipmentId = equipment?.equipmentId,
                    name = name,
                    category = category,
                    description = description,
                    totalQuantity = totalQty,
                    maxPerStudent = maxPerStudent,
                    maxDurationHours = maxDuration
                )

                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Equipment record saved successfully.", Toast.LENGTH_SHORT).show()
                        loadData()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error.message ?: "Failed to save equipment.", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleToggleActive(equipment: Equipment) {
        val actionText = if (equipment.isActive) "deactivate" else "activate"
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Action")
            .setMessage("Are you sure you want to $actionText '${equipment.name}'?")
            .setPositiveButton("Yes") { _, _ ->
                val result = SportsRepository.toggleEquipmentActive(equipment.equipmentId)
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Equipment status updated.", Toast.LENGTH_SHORT).show()
                        loadData()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error.message ?: "Action failed.", Toast.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun handleDelete(equipment: Equipment) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Equipment?")
            .setMessage("Are you sure you want to permanently delete '${equipment.name}'? (Note: Deletion is prohibited if reservations exist).")
            .setPositiveButton("Delete") { _, _ ->
                val result = SportsRepository.deleteEquipment(equipment.equipmentId)
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Equipment deleted successfully.", Toast.LENGTH_SHORT).show()
                        loadData()
                    },
                    onFailure = { error ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Deletion Prohibited")
                            .setMessage(error.message ?: "Cannot delete equipment.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
