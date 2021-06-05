package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_suggestion

import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.club.ClubSuggestion
import com.myfzone_sport.myf_zone.util.Constants.CLUB_SUGGESTION_PATH
import com.myfzone_sport.myf_zone.util.Constants.DB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Created by Amadou on 24/04/2021, 19:43
 *
 * Affiliation Suggestion Page Service
 *
 */

object AffiliationSuggestionService {
    fun createSuggestion(clubSuggestion: ClubSuggestion) =
        flow<State<ClubSuggestion>> {
            val mEventQuery = DB.collection(CLUB_SUGGESTION_PATH)
            clubSuggestion.id = mEventQuery.document().id

            emit(State.loading())

            mEventQuery.document(clubSuggestion.id).set(clubSuggestion.toMapSug()).await()

            emit(State.success(clubSuggestion))
        }.catch {
            emit(State.failed(it.localizedMessage?.toString() ?: it.message.toString()))
        }.flowOn(Dispatchers.IO)
}