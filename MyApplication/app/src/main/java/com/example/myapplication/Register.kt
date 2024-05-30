package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and Database
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Set click listener for the sign-up button
        binding.buttonSignUp.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            // Validation (Optional but recommended)
            if (name.isEmpty()) {
                binding.editTextName.error = "Name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.editTextEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password is required"
                return@setOnClickListener
            }

            // Call the signUpUser method with the user inputs
            signUpUser(name, email, password)
        }
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    // Method to save user data to Firebase Authentication and Realtime Database
    private fun signUpUser(name: String, email: String, password: String) {
        // Create user with email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val user = firebaseAuth.currentUser
                    user?.let {
                        // Save additional user data to Realtime Database
                        val userData = UserData(name, email, password)
                        firebaseDatabase.reference.child("users").child(user.uid).setValue(userData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@Register,
                                    "User signed up successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Navigate to the login page
                                val intent = Intent(this@Register, Login::class.java)
                                startActivity(intent)
                                finish() // Optional: Finish the current activity to prevent the user from returning to it using the back button
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@Register,
                                    "Failed to sign up user: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    // Registration failed
                    Toast.makeText(
                        this@Register,
                        "Failed to sign up user: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}