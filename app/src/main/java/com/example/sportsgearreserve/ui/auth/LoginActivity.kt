package com.example.sportsgearreserve.ui.auth

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sportsgearreserve.data.FirebaseManager
import com.example.sportsgearreserve.data.SessionManager
import com.example.sportsgearreserve.data.SportsRepository
import com.example.sportsgearreserve.databinding.ActivityLoginBinding
import com.example.sportsgearreserve.models.User
import com.example.sportsgearreserve.models.UserRole
import com.example.sportsgearreserve.ui.staff.StaffMainActivity
import com.example.sportsgearreserve.ui.student.StudentMainActivity
import com.google.android.material.tabs.TabLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private var currentMode = MODE_STUDENT_SIGNIN

    companion object {
        private const val MODE_STUDENT_SIGNIN = 0
        private const val MODE_STUDENT_REGISTER = 1
        private const val MODE_STAFF_PORTAL = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // If already logged in, redirect to appropriate main activity
        sessionManager.getUser()?.let { user ->
            navigateToHome(user)
            return
        }

        setupPrefixDropdown()
        setupTabListeners()
        setupClickListeners()
    }

    private fun setupPrefixDropdown() {
        val prefixes = arrayOf("Mr.", "Ms.", "Mrs.", "Dr.", "Prof.")
        val adapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, prefixes)
        binding.actvPrefix.setAdapter(adapter)
    }

    private fun setupTabListeners() {
        binding.tabLayoutMode.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentMode = tab?.position ?: MODE_STUDENT_SIGNIN
                updateUIForMode()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateUIForMode() {
        when (currentMode) {
            MODE_STUDENT_SIGNIN -> {
                binding.layoutRegisterFields.visibility = View.GONE
                binding.tvRoleNote.visibility = View.GONE
                binding.btnSubmit.text = "Sign In as Student"
                binding.etEmail.setText("student@uc.edu.ph")
            }
            MODE_STUDENT_REGISTER -> {
                binding.layoutRegisterFields.visibility = View.VISIBLE
                binding.tvRoleNote.visibility = View.GONE
                binding.btnSubmit.text = "Register Student Account"
                binding.etEmail.setText("")
            }
            MODE_STAFF_PORTAL -> {
                binding.layoutRegisterFields.visibility = View.GONE
                binding.tvRoleNote.visibility = View.VISIBLE
                binding.btnSubmit.text = "Sign In to Staff Portal"
                binding.etEmail.setText("staff@uc.edu.ph")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            handleFormSubmit()
        }
    }

    private fun handleFormSubmit() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please fill in email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        when (currentMode) {
            MODE_STUDENT_SIGNIN -> {
                val user = SportsRepository.getUserByEmail(email)
                if (user != null && user.role == UserRole.STUDENT) {
                    FirebaseManager.signIn(email, password) { _ ->
                        sessionManager.saveUser(user)
                        navigateToHome(user)
                    }
                } else {
                    Toast.makeText(this, "Student account not found with email: $email", Toast.LENGTH_LONG).show()
                }
            }
            MODE_STUDENT_REGISTER -> {
                val prefix = binding.actvPrefix.text.toString().trim()
                val firstName = binding.etFirstName.text.toString().trim()
                val middleName = binding.etMiddleName.text.toString().trim()
                val lastName = binding.etLastName.text.toString().trim()
                val studentId = binding.etStudentId.text.toString().trim()

                val result = SportsRepository.registerStudent(prefix, firstName, middleName, lastName, email, studentId)
                result.fold(
                    onSuccess = { newUser ->
                        FirebaseManager.signUp(email, password) { _ ->
                            Toast.makeText(this, "Account created & authenticated via Firebase!", Toast.LENGTH_SHORT).show()
                            sessionManager.saveUser(newUser)
                            navigateToHome(newUser)
                        }
                    },
                    onFailure = { err ->
                        Toast.makeText(this, err.message ?: "Registration failed.", Toast.LENGTH_LONG).show()
                    }
                )
            }
            MODE_STAFF_PORTAL -> {
                val user = SportsRepository.getUserByEmail(email)
                if (user != null && user.role == UserRole.STAFF) {
                    FirebaseManager.signIn(email, password) { _ ->
                        sessionManager.saveUser(user)
                        navigateToHome(user)
                    }
                } else {
                    Toast.makeText(this, "Unauthorized: Staff accounts cannot be self-registered. Use staff@uc.edu.ph or demo staff button.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToHome(user: User) {
        val intent = if (user.role == UserRole.STAFF) {
            Intent(this, StaffMainActivity::class.java)
        } else {
            Intent(this, StudentMainActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
