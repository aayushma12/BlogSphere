package com.example.blogapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ArticleItemBinding
import java.util.ArrayList

class ArticleAdapter(
    private val context: Context,
    private var blogList: List<BlogItemModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ArticleAdapter.BlogViewHolder>() {

    // Interface for handling item click events
    interface OnItemClickListener {
        fun onEditClick(blogItem: BlogItemModel)
        fun onReadMoreClick(blogItem: BlogItemModel)
        fun onDeleteClick(blogItem: BlogItemModel)
    }

    // Create a new view holder
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArticleItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    // Bind data to the views
    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = blogList[position]
        holder.bind(blogItem)
    }

    // Get the size of the data
    override fun getItemCount(): Int {
        return blogList.size
    }

    // Method to update the blog list and notify adapter
    fun setData(blogSavedList: ArrayList<BlogItemModel>) {
        this.blogList = blogSavedList
        notifyDataSetChanged()
    }

    // ViewHolder class for binding the layout
    inner class BlogViewHolder(private val binding: ArticleItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // Bind data to the views
        fun bind(blogItem: BlogItemModel) {
            // Set the data to the corresponding views
            binding.heading.text = blogItem.heading
            Glide.with(binding.profile.context)
                .load(blogItem.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItem.userName
            binding.date.text = blogItem.date
            binding.post.text = blogItem.post

            // Handle the 'Read More' button click
            binding.readmorebutton.setOnClickListener {
                itemClickListener.onReadMoreClick(blogItem)
            }

            // Handle the 'Edit' button click
            binding.editbutton.setOnClickListener {
                itemClickListener.onEditClick(blogItem)
            }

            // Handle the 'Delete' button click
            binding.deleteButton.setOnClickListener {
                itemClickListener.onDeleteClick(blogItem)
            }
        }
    }
}
