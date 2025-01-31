package com.example.blogapp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.example.blogapp.databinding.ActivityMainBinding
import com.google.firebase.database.collection.LLRBNode.Color


class MainActivity : AppCompatActivity() {
    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this,AddArticleActivity::class.java))
        }



    }

}