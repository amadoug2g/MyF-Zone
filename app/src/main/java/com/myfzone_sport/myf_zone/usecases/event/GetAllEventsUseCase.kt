package com.myfzone_sport.myf_zone.usecases.event

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import kotlinx.coroutines.flow.Flow

/**
 * Created by Amadou on 03/09/2021, 19:03
 */

class GetAllEventsUseCase(val repository: Repository) {
    operator fun invoke() = repository.getAllEvents()
}