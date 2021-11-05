package com.myfzone_sport.myf_zone.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.domain.sport.Category
import com.myfzone_sport.myf_zone.domain.sport.Sport

/**
 * Created by Amadou on 04/11/2021, 21:38
 */

class AffiliationCategoryAdapter(context: Context, var categoryList: MutableList<Category>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getItem(position: Int): Any {
        return categoryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val item: ItemHolder
        val category = categoryList[position]

        if (convertView == null) {
            view = inflater.inflate(R.layout.simple_layout_file, parent, false)
            item = ItemHolder(view)
            view?.tag = item
        } else {
            view = convertView
            item = view.tag as ItemHolder
        }

        item.categoryName.text = category.name

        return view
    }

    private class ItemHolder(row: View?) {
        val categoryName: TextView = row?.findViewById(R.id.name) as TextView
    }
}