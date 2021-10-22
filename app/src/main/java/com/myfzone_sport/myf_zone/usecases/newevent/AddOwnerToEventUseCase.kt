package com.myfzone_sport.myf_zone.usecases.newevent

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner

class AddOwnerToEventUseCase(val repository: Repository) {
    operator fun invoke(event: Event, owner: EventOwner) = repository.addOwnerToEvent(event, owner)
}