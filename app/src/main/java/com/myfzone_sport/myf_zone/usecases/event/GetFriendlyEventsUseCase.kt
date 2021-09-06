package com.myfzone_sport.myf_zone.usecases.event

import com.myfzone_sport.myf_zone.data.Repository

class GetFriendlyEventsUseCase(private val repository: Repository) {
    operator fun invoke() = repository.getFriendlyEvents()
}