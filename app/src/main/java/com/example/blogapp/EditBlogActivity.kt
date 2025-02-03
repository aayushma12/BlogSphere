package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityEditBlogBinding
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {
    private val binding : ActivityEditBlogBinding by lazy {
        ActivityEditBlogBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.imageButton.setOnClickListener {
            finish()
        }
        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")

        binding.BlogTitle.editText?.setText(blogItemModel?.heading)
        binding.BlogDescription.editText?.setText(blogItemModel?.post)


        binding.saveBlogButton.setOnClickListener {
            val updatedTitle= binding.BlogTitle.editText?.text.toString().trim()
            val updateDescription = binding.BlogDescription.editText?.text.toString().trim()
            
            if(updatedTitle.isEmpty() || updateDescription.isEmpty()){
                Toast.makeText(this, "Please Fill All The Details", Toast.LENGTH_SHORT).show()
            }else{
                blogItemModel?.heading= updatedTitle
                blogItemModel?.post=updateDescription

                if(blogItemModel != null){
                    updatedDataInFirebase(blogItemModel)
                }
            }
        }
    }

    private fun updatedDataInFirebase(blogItemModel: BlogItemModel) {
val databaseReference= FirebaseDatabase.getInstance().getReference("blogs")
        val postId = blogItemModel.postId
        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Blog Update Failed", Toast.LENGTH_SHORT).show()
            }
    }
}