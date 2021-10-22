package com.myfzone_sport.myf_zone.usecases.newevent

import com.myfzone_sport.myf_zone.data.Repository

class GetOwnerForNewEventUseCase(val repository: Repository) {
    operator fun invoke() = repository.getOwnerForNewEvent()
}