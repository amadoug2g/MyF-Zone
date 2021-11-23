package com.myfzone_sport.myf_zone.usecases.discussion

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.coach.Coach

class SetDiscussionUnreadUseCase(val repository: Repository) {
    operator fun invoke(chatCoach: Coach) = repository.setDiscussionUnread(chatCoach)
}