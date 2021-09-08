package com.myfzone_sport.myf_zone.usecases.event

import com.myfzone_sport.myf_zone.data.Repository

class GetUserEventsUseCase(val repository: Repository)  {
    operator fun invoke() = repository.getUserEvents()
}