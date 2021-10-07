package com.myfzone_sport.myf_zone.usecases.detailevent

import com.myfzone_sport.myf_zone.data.Repository

class GetEventFromIdUseCase (val repository: Repository) {
    operator fun invoke(eventId: String) = repository.getEventFromId(eventId)
}