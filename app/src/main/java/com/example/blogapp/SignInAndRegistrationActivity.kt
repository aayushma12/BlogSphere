package com.example.blogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.util.*

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    // Cloudinary Configuration
    private val cloudinaryConfig = mapOf(
        "cloud_name" to "djsc3vi6q",  // Replace with your Cloudinary cloud name
        "api_key" to "763563615486535",        // Replace with your Cloudinary API key
        "api_secret" to "x5ogs5QtikGGgxj9HR6Pb4sWIzs"   // Replace with your Cloudinary API secret
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase Authentication and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize Cloudinary
        MediaManager.init(this, cloudinaryConfig)

        // For visibility of fields
        val action = intent.getStringExtra("action")

        // Adjust visibility for login
        if (action == "login") {
            binding.loginEmailAddress.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE

            binding.registerButton.isEnabled = false
            binding.registerButton.alpha = 0.5f
            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f
            binding.registerEmail.visibility = View.GONE
            binding.registerName.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE

            binding.loginButton.setOnClickListener {
                val loginEmail = binding.loginEmailAddress.text.toString()
                val loginPassword = binding.loginPassword.text.toString()
                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Login Failed, Please Enter correct Details", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

        } else if (action == "register") {
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

            binding.registerButton.setOnClickListener {
                // Get data from edit text fields
                val registerName = binding.registerName.text.toString()
                val registerEmail = binding.registerEmail.text.toString()
                val registerPassword = binding.registerPassword.text.toString()
                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
                } else {

                    auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                auth.signOut()
                                user?.let {
                                    val userReference = database.getReference("users")
                                    val userId = user.uid
                                    val userData = UserData(
                                        registerName,
                                        registerEmail
                                    )
                                    userReference.child(userId).setValue(userData)

                                    // Upload image to Cloudinary
                                    if (imageUri != null) {
                                        val options = HashMap<String, Any>()
                                        options["public_id"] = "profile_image/$userId.jpg"

                                        // Upload image to Cloudinary
                                        MediaManager.get().upload(imageUri!!)
                                            .options(options)
                                            .callback(object : UploadCallback {
                                                override fun onStart(requestId: String?) {
                                                    // Code when upload starts (optional)
                                                }

                                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                                                    // Optionally update progress (optional)
                                                }

                                                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                                    // Success! Get the URL and save to Firebase Realtime Database
                                                    val imageUrl = resultData?.get("secure_url").toString()
                                                    userReference.child(userId).child("profileImage").setValue(imageUrl)

                                                    Toast.makeText(this@SignInAndRegistrationActivity, "User Register Success", Toast.LENGTH_SHORT).show()
                                                    startActivity(Intent(this@SignInAndRegistrationActivity, WelcomeActivity::class.java))
                                                    finish()
                                                }

                                                // Correct method signature for onError
                                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                                    // Handle error here with the ErrorInfo object
                                                    error?.let {
                                                        Toast.makeText(this@SignInAndRegistrationActivity, "Error Uploading Image: ${it.description}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                                                    // Handle reschedule here (optional)
                                                    error?.let {
                                                        Log.d("Cloudinary", "Reschedule requested for $requestId, error: ${it.description}")
                                                    }
                                                }
                                            }).dispatch()
                                    }

                                }
                            } else {
                                Toast.makeText(this, "User Registration Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        // Set on click listener for Choose image
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                PICK_IMAGE_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null)
            imageUri = data.data

        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.registerUserImage)
    }
}
