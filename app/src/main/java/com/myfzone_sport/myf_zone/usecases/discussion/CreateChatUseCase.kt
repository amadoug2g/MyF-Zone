package com.myfzone_sport.myf_zone.usecases.discussion

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation
import com.myfzone_sport.myf_zone.domain.coach.Coach

class CreateChatUseCase(val repository: Repository) {
    operator fun invoke(chatCoach: Coach, chatCoachClub: ClubAffiliation) = repository.createChat(chatCoach, chatCoachClub)
}