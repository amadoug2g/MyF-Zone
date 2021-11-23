package com.myfzone_sport.myf_zone.usecases.discussion

import com.myfzone_sport.myf_zone.data.Repository

class GetChatCoachClubUseCase(val repository: Repository) {
    operator fun invoke(chatCoachId: String) = repository.getDiscussionUserClub(chatCoachId)
}