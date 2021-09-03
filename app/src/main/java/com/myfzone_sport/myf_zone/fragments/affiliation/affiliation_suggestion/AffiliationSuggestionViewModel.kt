package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_suggestion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import com.myfzone_sport.myf_zone.domain.club.ClubSuggestion

/**
 * Created by Amadou on 24/04/2021, 19:43
 *
 * Affiliation Suggestion ViewModel class
 *
 */

class AffiliationSuggestionViewModel : ViewModel() {
    private val _fields = MutableLiveData<MutableList<Place.Field>>()
    val fields: LiveData<MutableList<Place.Field>>
        get() = _fields

    private fun initFields() {
        _fields.value = mutableListOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
    }

    init {
        initFields()
    }

    fun createSuggestion(clubSuggestion: ClubSuggestion) =
        AffiliationSuggestionService.createSuggestion(
            clubSuggestion
        )
}