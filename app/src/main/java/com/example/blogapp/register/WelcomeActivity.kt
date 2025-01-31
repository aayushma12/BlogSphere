package com.example.blogapp.register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.blogapp.SignInAndRegistrationActivity
import com.example.blogapp.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private val binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val intent =Intent(this,SignInAndRegistrationActivity::class.java)
            intent.putExtra("action","login")
            startActivity(intent)

        }

        binding.RegisterButton.setOnClickListener {
            val intent =Intent(this,SignInAndRegistrationActivity::class.java)
            intent.putExtra("action","register")
            startActivity(intent)

        }
    }
}
