package com.myfzone_sport.myf_zone.usecases.newevent

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventOwner

class AddNewEventToUserUseCase(val repository: Repository) {
    operator fun invoke(event: Event, owner: EventOwner, club: ClubAffiliation) = repository.addNewEventToUser(event, owner, club)
}