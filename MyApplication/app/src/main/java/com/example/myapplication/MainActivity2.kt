package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMain2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private lateinit var tableLayout: TableLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve UI components
        tableLayout = binding.tableLayout1

        // Load data from Firebase to display in the table
        loadDataFromFirebase()

        binding.homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.incomeBtn.setOnClickListener {
            val intent = Intent(this, Incomes::class.java)
            startActivity(intent)
        }
        binding.expencesBtnOutcome.setOnClickListener {
            val intent = Intent(this, OutComes::class.java)
            startActivity(intent)
        }
        binding.historyBtn.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        binding.deleteBtn.setOnClickListener {
            clearDataFromFirebase()
        }
    }

    private fun loadDataFromFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val outcomesRef = database.getReference("users").child(userId).child("transferDetails")

            outcomesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    tableLayout.removeAllViews()
                    for (dataSnapshot in snapshot.children) {
                        val outcome = dataSnapshot.getValue(OutcomeData::class.java)
                        if (outcome != null) {
                            addTableRow(outcome)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity2, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearDataFromFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val outcomesRef = database.getReference("users").child(userId).child("transferDetails")

            outcomesRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Data cleared successfully", Toast.LENGTH_SHORT).show()
                    tableLayout.removeAllViews()
                } else {
                    Toast.makeText(this, "Failed to clear data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
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
            setPadding(16, 8, 8, 8)
            textSize = 24f
        }

        val dateView = TextView(this).apply {
            id = View.generateViewId()
            text = outcome.date
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(8, 8, 8, 8)
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val amountView = TextView(this).apply {
            id = View.generateViewId()
            text = outcome.amount
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(8, 8, 16, 8)
            textSize = 24f
            textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            setTextColor(if (outcome.description == "Income") Color.GREEN else Color.RED)
            gravity = Gravity.END
        }


        tableRow.addView(descriptionView)
        tableRow.addView(dateView)
        tableRow.addView(amountView)

        tableRow.setOnClickListener {
            val intent = Intent(this, OutComes::class.java).apply {
                putExtra("OUTCOME_ID", outcome.id)
            }
            startActivity(intent)
        }

        tableLayout.addView(tableRow)
    }


    data class OutcomeData(
        val id: String = "",
        val date: String = "",
        val amount: String = "",
        val description: String = "",
        val type: String = ""
    )
}
