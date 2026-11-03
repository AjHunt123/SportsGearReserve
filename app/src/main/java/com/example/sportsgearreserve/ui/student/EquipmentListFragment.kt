package com.example.sportsgearreserve.ui.student

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sportsgearreserve.data.SportsRepository
import com.example.sportsgearreserve.databinding.FragmentEquipmentListBinding
import com.example.sportsgearreserve.models.Equipment
import com.example.sportsgearreserve.ui.adapters.EquipmentAdapter
import com.google.android.material.chip.Chip

class EquipmentListFragment : Fragment() {

    private var _binding: FragmentEquipmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EquipmentAdapter
    private var allEquipment = listOf<Equipment>()
    private var selectedCategory = "All"
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEquipmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = EquipmentAdapter(
            equipmentList = emptyList(),
            isStaffMode = false,
            onItemClick = { equipment ->
                val intent = Intent(requireContext(), EquipmentDetailActivity::class.java)
                intent.putExtra("EQUIPMENT_ID", equipment.equipmentId)
                startActivity(intent)
            }
        )
        binding.rvEquipmentList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEquipmentList.adapter = adapter
    }

    private fun setupCategoryChips() {
        val categories = SportsRepository.getCategories()
        binding.chipGroupCategory.removeAllViews()

        for (cat in categories) {
            val chip = Chip(requireContext()).apply {
                text = cat
                isCheckable = true
                isChecked = (cat == "All")
                setOnClickListener {
                    selectedCategory = cat
                    filterEquipment()
                }
            }
            binding.chipGroupCategory.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                filterEquipment()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadData() {
        allEquipment = SportsRepository.getAllEquipment(includeInactive = false)
        filterEquipment()
    }

    private fun filterEquipment() {
        val filtered = allEquipment.filter { item ->
            val matchesCategory = (selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isEmpty() || item.name.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvEquipmentList.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvEquipmentList.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
