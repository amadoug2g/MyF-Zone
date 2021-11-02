package com.myfzone_sport.myf_zone.usecases.detailevent

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.event.EventParticipant

class RefuseParticipantUseCase(val repository: Repository) {
    operator fun invoke(eventId: String, participant: EventParticipant) = repository.refuseParticipant(eventId, participant)
}