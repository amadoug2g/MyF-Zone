package com.myfzone_sport.myf_zone.app.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import org.jetbrains.anko.image

/**
 * Created by Amadou on 03/11/2021, 14:26
 */

class AffiliationClubAdapter(context: Context, var clubList: MutableList<Club>, private val getImageReferenceUseCase: GetImageReferenceUseCase) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return clubList.size
    }

    override fun getItem(position: Int): Any {
        return clubList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val item: ItemHolder
        val club = clubList[position]

        if (convertView == null) {
            view = inflater.inflate(R.layout.simple_club_layout_file, parent, false)
            item = ItemHolder(view)
            view?.tag = item
        } else {
            view = convertView
            item = view.tag as ItemHolder
        }

        if (club.acronym != "MFZ" && club.acronym != "MFZGuest") {
            item.clubName.text = club.name

            GlideApp.with(view).apply {
                load(getImageReferenceUseCase.invoke(club.logo))
                    .centerCrop()
                    .into(item.clubImage)
            }
        }

        return view
    }

    private class ItemHolder(row: View?) {
        val clubName: TextView = row?.findViewById(R.id.club_name) as TextView
        val clubImage: ImageView = row?.findViewById(R.id.club_image) as ImageView
    }
}