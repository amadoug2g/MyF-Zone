package com.myfzone_sport.myf_zone.usecases.event

import com.myfzone_sport.myf_zone.data.Repository

/**
 * Created by Amadou on 08/09/2021, 20:10
 */

class GetCloseEventsUseCase(private val repository: Repository) {
    operator fun invoke() = repository.getCloseEvents()
}