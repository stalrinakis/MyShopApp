package com.example.myshopapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myshopapp.R
import com.example.myshopapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.tvAlreadyRegistered.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnRegister.setOnClickListener {
            val firstname = binding.etFirstName.text.toString()
            val lastname = binding.etLastName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confpass = binding.etConfirmPassword.text.toString()

            // Check if any of the fields are empty
            if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty() || confpass.isEmpty()) {
                Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if password and confirm password match
            if (password != confpass) {
                Toast.makeText(this, R.string.pass_not_match, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proceed with Firebase Authentication
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    Toast.makeText(this, R.string.registration_successful, Toast.LENGTH_SHORT).show()

                    // Get the current user's UID
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        val user = hashMapOf(
                            "firstName" to firstname,
                            "lastName" to lastname,
                            "email" to email
                        )

                        // Add a new document with the user's UID
                        db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                // Handle success
                                println("DocumentSnapshot added with ID: $uid")
                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                println("Error adding document: $e")
                            }

                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }

                } else {
                    // Registration failed
                    Toast.makeText(this, "${getString(R.string.registration_failed)}: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
