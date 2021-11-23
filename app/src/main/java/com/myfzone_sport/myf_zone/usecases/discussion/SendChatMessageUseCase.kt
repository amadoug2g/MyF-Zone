package com.myfzone_sport.myf_zone.usecases.discussion

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.coach.Coach

class SendChatMessageUseCase(val repository: Repository) {
    operator fun invoke(chatCoach: Coach, message: String, photo: String) =
        repository.sendChatMessage(chatCoach, message, photo)
}