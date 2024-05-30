package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var totalAmountTextView: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var usernameTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve UI components
        tableLayout = findViewById(R.id.tableLayout1)
        totalAmountTextView = findViewById(R.id.total_amount)
        usernameTextView = findViewById(R.id.username_txt) // Retrieve the username TextView

        // Load data from Firebase to display in the table
        loadDataFromFirebase()
        loadUsernameFromFirebase() // Load the username from Firebase

        // Set onClickListeners for navigation buttons
        val incomeBTN = findViewById<ImageView>(R.id.incomeBTN)
        incomeBTN.setOnClickListener {
            val intent = Intent(this, Incomes::class.java)
            startActivity(intent)
        }

        val outcome = findViewById<ImageView>(R.id.expenses_btn)
        outcome.setOnClickListener {
            val intent = Intent(this, OutComes::class.java)
            startActivity(intent)
        }

        val transfaction = findViewById<ImageView>(R.id.transfaction_btn)
        transfaction.setOnClickListener {
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
    }

    private fun loadDataFromFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val outcomesRef = database.getReference("users").child(userId).child("transferDetails")

            var totalAmount = 0.0 // Initialize total amount
            var rowCount = 0

            outcomesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tableLayout.removeAllViews()
                    totalAmount = 0.0 // Reset total amount before recalculating
                    rowCount = 0 // Reset row count
                    for (dataSnapshot in snapshot.children) {
                        val outcome = dataSnapshot.getValue(OutcomeData::class.java)
                        if (outcome != null) {
                            totalAmount += if (outcome.description == "Income") outcome.amount.toDouble() else -outcome.amount.toDouble()
                        }
                    }
                    for (dataSnapshot in snapshot.children) {
                        val outcome = dataSnapshot.getValue(OutcomeData::class.java)
                        if (outcome != null) {
                            addTableRow(outcome)
                            // Increment row count
                            rowCount++
                            // Check if 10 rows have been added
                            if (rowCount >= 10) {
                                break // Exit the loop
                            }
                        }
                    }
                    // Update the total amount view
                    updateTotalAmount(totalAmount)
                    loadUsernameFromFirebase()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
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
                        usernameTextView.text = username
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed to load username: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTotalAmount(totalAmount: Double) {
        val formattedTotalAmount = String.format("%.2f", totalAmount)
        totalAmountTextView.text = formattedTotalAmount
    }

    private fun addTableRow(outcome: OutcomeData) {
        val tableRow = TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val descriptionView = TextView(this).apply {
            id = View.generateViewId()
            text = outcome.type
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                2f
            )
            setPadding(10, 0, 0, 0)
            textSize = 28f
        }

        val amountView = TextView(this).apply {
            id = View.generateViewId()
            text = outcome.amount
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(0, 0, 10, 0)
            textSize = 28f
            textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            setTextColor(if (outcome.description == "Income") Color.GREEN else Color.RED)
            gravity = Gravity.END
        }

        tableRow.addView(descriptionView)
        tableRow.addView(amountView)
        tableLayout.addView(tableRow)
    }

    data class OutcomeData(
        val date: String = "",
        val amount: String = "",
        val description: String = "",
        val type: String = ""
    )
}
