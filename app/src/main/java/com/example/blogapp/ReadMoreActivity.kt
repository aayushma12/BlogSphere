package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")

        if(blogs != null){

            // Retrive user related data here e.x blog title etc.
            binding.titleText.text = blogs.heading
            binding.userName.text= blogs.userName
            binding.dateReadMore.text= blogs.date
            binding.blogDescriptionTextView.text= blogs.post
        }
        else{
            Toast.makeText(this@ReadMoreActivity, "Failed to load blogs", Toast.LENGTH_SHORT).show()
        }

    }
}