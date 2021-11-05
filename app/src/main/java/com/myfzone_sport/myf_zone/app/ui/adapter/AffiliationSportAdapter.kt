package com.myfzone_sport.myf_zone.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.domain.sport.Sport
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase

/**
 * Created by Amadou on 04/11/2021, 21:32
 */

class AffiliationSportAdapter(context: Context, var sportList: MutableList<Sport>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return sportList.size
    }

    override fun getItem(position: Int): Any {
        return sportList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val item: ItemHolder
        val sport = sportList[position]

        if (convertView == null) {
            view = inflater.inflate(R.layout.simple_layout_file, parent, false)
            item = ItemHolder(view)
            view?.tag = item
        } else {
            view = convertView
            item = view.tag as ItemHolder
        }

        item.sportName.text = sport.name

        return view
    }

    private class ItemHolder(row: View?) {
        val sportName: TextView = row?.findViewById(R.id.name) as TextView
    }
}