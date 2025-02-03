package com.example.blogapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.ArticleAdapter
import com.example.blogapp.databinding.ActivityArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ArticleActivity : AppCompatActivity() {
    private val binding: ActivityArticleBinding by lazy {
        ActivityArticleBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var blogAdapter: ArticleAdapter
    private val EDIT_BLOG_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()  // Close the activity if user is not logged in
            return
        }

        binding.backButton.setOnClickListener {
            finish()
        }
        val recyclerView = binding.articleRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        if(currentUserId!= null){
            blogAdapter = ArticleAdapter(this, emptyList(), object : ArticleAdapter.OnItemClickListener {
                override fun onEditClick(blogItem: BlogItemModel) {
                    val intent = Intent(this@ArticleActivity,EditBlogActivity::class.java)
                    intent.putExtra("blogItem", blogItem)
                    startActivityForResult(intent, EDIT_BLOG_REQUEST_CODE)
                }

                override fun onReadMoreClick(blogItem: BlogItemModel) {
                    val intent = Intent(this@ArticleActivity,ReadMoreActivity::class.java)
                    intent.putExtra("blogItem", blogItem)
                    startActivity(intent)
                }

                override fun onDeleteClick(blogItem: BlogItemModel) {
                    deleteBlogPost(blogItem)
                }
            })
        }


        recyclerView.adapter = blogAdapter

        // Get saved blog data from Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("blogs")

        // Add listener to get blog posts
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogSavedList = ArrayList<BlogItemModel>()
                for (postSnapshot in snapshot.children) {
                    val blogSaved = postSnapshot.getValue(BlogItemModel::class.java)
                    if (blogSaved != null && blogSaved.userId == currentUserId) {
                        blogSavedList.add(blogSaved)
                    }
                }

                // Log the blog list size to ensure data is fetched
                Log.d("ArticleActivity", "Blogs loaded: ${blogSavedList.size}")
                blogAdapter.setData(blogSavedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArticleActivity, "Error loading saved blogs", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteBlogPost(blogItem: BlogItemModel) {
        val postId = blogItem.postId
        val blogPostReference = databaseReference.child(postId)

        // Remove the blog post
        blogPostReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Post Deleted Successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Blog Post Deletion Failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==EDIT_BLOG_REQUEST_CODE && resultCode== Activity.RESULT_OK){

        }
    }
}
