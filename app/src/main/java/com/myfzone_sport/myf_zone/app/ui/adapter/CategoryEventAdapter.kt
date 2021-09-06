package com.myfzone_sport.myf_zone.app.ui.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.HomeCategoryCardviewBinding
import com.myfzone_sport.myf_zone.domain.event.Event

/**
 * Created by Amadou on 04/09/2021, 02:19
 */

class CategoryEventAdapter : RecyclerView.Adapter<CategoryEventAdapter.MyViewHolder>() {
    private var categoryList = emptyList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = categoryList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun setData(list: List<String>) {
        this.categoryList = list
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: HomeCategoryCardviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String) {
            with(binding) {
                binding.category = category
            }

            binding.cardView.setOnClickListener {
                when (category) {
                    "Matches Amicaux" -> {
                        val bundle = bundleOf("listType" to "friendly")
                        navigate(R.id.homeFragmentToCategoryListFragment, bundle, binding.cardView)
                    }
                    "Tournois" -> {
                        val bundle = bundleOf("listType" to "tourney")
                        navigate(R.id.homeFragmentToCategoryListFragment, bundle, binding.cardView)
                    }
                    "Plateaux" -> {
                        val bundle = bundleOf("listType" to "plateau")
                        navigate(R.id.homeFragmentToCategoryListFragment, bundle, binding.cardView)
                    }
                }
            }
        }

        private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
            Navigation
                .findNavController(view)
                .navigate(destination, extra)
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    HomeCategoryCardviewBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }
}