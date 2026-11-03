package com.example.sportsgearreserve.ui.staff

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sportsgearreserve.R
import com.example.sportsgearreserve.data.SessionManager
import com.example.sportsgearreserve.databinding.ActivityStaffMainBinding
import com.example.sportsgearreserve.ui.auth.LoginActivity

class StaffMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        binding.toolbar.subtitle = "Staff: ${user?.fullName ?: "Sports Admin"}"
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_logout) {
                sessionManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            } else {
                false
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_staff_queue -> {
                    loadFragment(StaffDashboardFragment())
                    true
                }
                R.id.nav_manage_equipment -> {
                    loadFragment(ManageEquipmentFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadFragment(StaffDashboardFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
