package com.example.myshopapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myshopapp.R
import com.example.myshopapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    // Lateinit variables for view binding and Firebase instances
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth and Firestore instances
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set onClickListener for "Not Registered" TextView
        binding.tvNotRegistered.setOnClickListener {
            // Navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListener for the login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Check if email or password fields are empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed with Firebase Authentication
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        // Fetch user details from Firestore
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val firstName = document.getString("firstName") ?: ""
                                    val lastName = document.getString("lastName") ?: ""
                                    val userEmail = document.getString("email") ?: ""

                                    // Display welcome message
                                    Toast.makeText(this, "${getString(R.string.welcome)} $firstName $lastName", Toast.LENGTH_SHORT).show()

                                    // Start MainActivity and pass user details
                                    val intent = Intent(this, MainActivity::class.java)
                                    intent.putExtra("firstName", firstName)
                                    intent.putExtra("lastName", lastName)
                                    intent.putExtra("email", userEmail)
                                    startActivity(intent)
                                } else {
                                    // User document does not exist
                                    Toast.makeText(this, R.string.user_not_found, Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                // Error fetching user details
                                Toast.makeText(this, "Error fetching user details: $e", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // UID is null
                        Toast.makeText(this, R.string.uid_not_found, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Login failed
                    Toast.makeText(this, "${getString(R.string.login_failed)}: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
