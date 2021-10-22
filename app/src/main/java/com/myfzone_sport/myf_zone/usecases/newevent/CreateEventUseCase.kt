package com.myfzone_sport.myf_zone.usecases.newevent

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.event.Event

class CreateEventUseCase(val repository: Repository) {
    operator fun invoke(event: Event) = repository.createEvent(event)
}