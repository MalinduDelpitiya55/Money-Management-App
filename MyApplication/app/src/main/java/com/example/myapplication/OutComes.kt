package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OutComes : AppCompatActivity() {

    private lateinit var currentName: TextView
    private lateinit var newName: TextInputLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_out_comes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve UI components
        currentName = findViewById(R.id.current_username)
        newName = findViewById(R.id.new_username)
        submitButton = findViewById(R.id.submit_btn)

        // Load data from Firebase to display in the table
        loadDataFromFirebase()
        loadUsernameFromFirebase() // Load the username from Firebase



        val incomes = findViewById<ImageView>(R.id.home_btn)
        incomes.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        val settings = findViewById<ImageView>(R.id.income_btn)
        settings.setOnClickListener {
            val intent = Intent(this, Incomes::class.java)
            startActivity(intent)
        }

        val income = findViewById<ImageView>(R.id.expences_btn_outcome)
        income.setOnClickListener {
            val intent = Intent(this, OutComes::class.java)
            startActivity(intent)
        }

        val imageView = findViewById<ImageView>(R.id.history_btn)
        imageView.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
        submitButton.setOnClickListener {
            handleChangeUsername()
        }
    }

    private fun loadDataFromFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val outcomesRef = database.getReference("users").child(userId).child("transferDetails")


            outcomesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    loadUsernameFromFirebase()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OutComes, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUsernameFromFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = database.getReference("users").child(userId).child("name") // Assuming the username is stored under "username"

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.getValue(String::class.java)
                    if (username != null) {
                        currentName.text = username
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@OutComes, "Failed to load username: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handleChangeUsername() {
        val newUsername = newName.editText?.text.toString().trim()

        if (newUsername.isEmpty()) {
            newName.error = getString(R.string.new_username_error)
            return
        }

        // Update the username in Firebase
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userRef = database.getReference("users").child(userId).child("name")

            userRef.setValue(newUsername).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.username_changed_success), Toast.LENGTH_LONG).show()
                    currentName.text = newUsername // Update the TextView with the new username
                } else {
                    Toast.makeText(this, "Failed to change username: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


    data class OutcomeData(
        val date: String = "",
        val amount: String = "",
        val description: String = "",
        val type: String = ""
    )
}
}
