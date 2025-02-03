package com.example.blogapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId ?: return
            val context = binding.root.context

            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likecount.text = blogItemModel.likeCount.toString()

            // Open full blog on click
            binding.root.setOnClickListener {
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            // Check if the current user has liked the post
            currentUser?.uid?.let { uid ->
                databaseReference.child("blogs").child(postId).child("likes").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val liked = snapshot.exists()
                            updateLikeButtonImage(binding, liked)
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            // Handle like button clicks
            binding.likeButton.setOnClickListener {
                if (currentUser != null) {
                    handelLikeButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You have to login first", Toast.LENGTH_SHORT).show()
                }
            }

            // Check if the post is saved
            currentUser?.uid?.let { uid ->
                databaseReference.child("users").child(uid).child("saveBlogPosts").child(postId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            updateSaveButtonImage(binding, snapshot.exists())
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            // Handle save button clicks
            binding.postsaveButton.setOnClickListener {
                if (currentUser != null) {
                    handelSaveButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You have to login first", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handelLikeButtonClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Unlike the post
                        userReference.child("likes").child(postId).removeValue()
                        postLikeReference.child(currentUser.uid).removeValue()
                        blogItemModel.likeCount -= 1
                        databaseReference.child("blogs").child(postId).child("likeCount")
                            .setValue(blogItemModel.likeCount)
                        updateLikeButtonImage(binding, false)
                    } else {
                        // Like the post
                        userReference.child("likes").child(postId).setValue(true)
                        postLikeReference.child(currentUser.uid).setValue(true)
                        blogItemModel.likeCount += 1
                        databaseReference.child("blogs").child(postId).child("likeCount")
                            .setValue(blogItemModel.likeCount)
                        updateLikeButtonImage(binding, true)
                    }
                    notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
        binding.likeButton.setImageResource(if (liked) R.drawable.redfill_heart else R.drawable.black_heart)
    }

    private fun handelSaveButtonClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postSaveReference = userReference.child("saveBlogPosts").child(postId)

        postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Unsave post
                    postSaveReference.removeValue()
                        .addOnSuccessListener {
                            updateSaveButtonImage(binding, false)
                            Toast.makeText(binding.root.context, "Blog Unsaved!", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Save post
                    postSaveReference.setValue(true)
                        .addOnSuccessListener {
                            updateSaveButtonImage(binding, true)
                            Toast.makeText(binding.root.context, "Blog Saved!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateSaveButtonImage(binding: BlogItemBinding, saved: Boolean) {
        binding.postsaveButton.setImageResource(if (saved) R.drawable.minus_bookmark else R.drawable.save_articles_fill_red)
    }

    fun updateData(savedBlogArticles: List<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogArticles)
        notifyDataSetChanged()
    }
}
