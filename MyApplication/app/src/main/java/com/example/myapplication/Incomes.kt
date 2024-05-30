package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class Incomes : AppCompatActivity() {

    private lateinit var dateInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var descriptionInput: TextInputEditText

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incomes)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve UI components
        dateInput = findViewById(R.id.date_input)
        amountInput = findViewById(R.id.amount_input)
        descriptionInput = findViewById(R.id.description_input)
        radioGroup = findViewById(R.id.radioGroup)

        // Button to submit data
        val submitBtn: Button = findViewById(R.id.submit_btn)
        submitBtn.setOnClickListener {
            if (validateDate()) {
                sendDataToFirebase()
            } else {
                Toast.makeText(this, "Please enter a valid date", Toast.LENGTH_SHORT).show()
            }
        }

        // Other button functionalities
        findViewById<ImageView>(R.id.home_btn).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<ImageView>(R.id.income_btn).setOnClickListener {
            startActivity(Intent(this, Incomes::class.java))
        }
        findViewById<ImageView>(R.id.expences_btn_outcome).setOnClickListener {
            startActivity(Intent(this, OutComes::class.java))
        }
        findViewById<ImageView>(R.id.history_btn).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }
    }

    private fun validateDate(): Boolean {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        dateFormat.isLenient = false
        val dateString = dateInput.text.toString().trim()
        return try {
            dateFormat.parse(dateString)
            true
        } catch (e: ParseException) {
            false
        }
    }

    private fun sendDataToFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val outcomesRef = database.getReference("users").child(userId).child("transferDetails")

            val date = dateInput.text.toString().ifEmpty { "01-01-2024" }
            val amount = amountInput.text.toString().ifEmpty { "0.0" }
            val description = descriptionInput.text.toString().ifEmpty { "No description" }


            val selectedType = when (findViewById<RadioButton>(radioGroup.checkedRadioButtonId)) {
                findViewById<RadioButton>(R.id.radio_income) -> "Income"
                findViewById<RadioButton>(R.id.radio_outcome) -> "Outcome"
                else -> "" // Default value if none is selected
            }

            val outcomeData = OutcomeData(date, amount,  description, selectedType)

            outcomesRef.push().setValue(outcomeData)
                .addOnSuccessListener {
                    // Handle successful data addition (e.g., show a toast message)
                    Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    // Handle failure in adding data (e.g., show an error message)
                    Toast.makeText(this, "Failed to submit data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


    data class OutcomeData(
        val date: String,
        val amount: String,
        val type: String,
        val description: String
    )
}
