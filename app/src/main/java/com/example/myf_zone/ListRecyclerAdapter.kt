package com.example.myf_zone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myf_zone.model.event.EventSection

class ListRecyclerAdapter(private val sectionList: MutableList<EventSection>) :
    RecyclerView.Adapter<ListRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.event_section_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = sectionList[position]

        val text = section.getSectionName()
        val list = section.getSectionList()

        holder.textView.text = text
        val childRecyclerAdapter = ChildRecyclerAdapter(list)
        holder.recyclerView!!.adapter = childRecyclerAdapter


    }

    override fun getItemCount(): Int {
        return sectionList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var textView = view.findViewById<TextView>(R.id.sectionTextView)
        var recyclerView = view.findViewById<RecyclerView>(R.id.parentRecyclerView)

//        var title: TextView = view.findViewById(R.id.title)
//        var year: TextView = view.findViewById(R.id.year)
//        var genre: TextView = view.findViewById(R.id.genre)
    }
}