package com.example.blogapp

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivitySavedArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedArticlesActivity : AppCompatActivity() {

    private val binding: ActivitySavedArticlesBinding by lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }

    private val savedBlogArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Prevent opening if user is not logged in
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize blogAdapter
        blogAdapter = BlogAdapter(savedBlogArticles.filter { it.isSaved }.toMutableList())

        val recyclerView = binding.savesArticelRecyclerview
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userReference = FirebaseDatabase.getInstance()
            .getReference("users").child(userId).child("saveBlogPosts")

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    val postId = postSnapshot.key
                    val isSaved = postSnapshot.getValue(Boolean::class.java) ?: false
                    if (postId != null && isSaved) {
                        // Fetch the corresponding blog item on postId using a coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            val blogItem = fetchBlogItem(postId)
                            if (blogItem != null) {
                                savedBlogArticles.add(blogItem)

                                launch(Dispatchers.Main) {
                                    blogAdapter.updateData(savedBlogArticles)
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference = FirebaseDatabase.getInstance()
            .getReference("blogs")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            dataSnapshot.getValue(BlogItemModel::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
