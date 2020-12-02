package mfz.myfzone_sport.myf_zone.model.event.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.event_section_row.view.*
import mfz.myfzone_sport.myf_zone.R

class ListRecyclerAdapter(private var sectionList: MutableList<EventSection>) :
    RecyclerView.Adapter<ListRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.event_section_row, parent, false)
        return ViewHolder(
            view
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val section = sectionList[position]

        val text = section.getSectionName()
        val list = section.getSectionList()

        holder.itemView.sectionNameTextView.text = text

        val childRecyclerAdapter =
            ChildRecyclerAdapter(list)
        holder.itemView.childRecyclerView.adapter = childRecyclerAdapter
    }

    override fun getItemCount(): Int {
        return sectionList.size
    }

    fun reloadList(list: MutableList<EventSection>) {
        sectionList = list
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView: TextView = view.sectionNameTextView
        var childRecyclerView: RecyclerView = view.childRecyclerView
    }
}
