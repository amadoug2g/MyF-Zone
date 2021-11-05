package com.myfzone_sport.myf_zone.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.domain.sport.SubCategory

/**
 * Created by Amadou on 05/11/2021, 00:06
 */

class AffiliationSubCategoryAdapter(context: Context, var subCategoryList: MutableList<SubCategory>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return subCategoryList.size
    }

    override fun getItem(position: Int): Any {
        return subCategoryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val item: ItemHolder
        val subCategory = subCategoryList[position]

        if (convertView == null) {
            view = inflater.inflate(R.layout.simple_layout_file, parent, false)
            item = ItemHolder(view)
            view?.tag = item
        } else {
            view = convertView
            item = view.tag as ItemHolder
        }

        item.subCategoryName.text = subCategory.name

        return view
    }

    private class ItemHolder(row: View?) {
        val subCategoryName: TextView = row?.findViewById(R.id.name) as TextView
    }
}