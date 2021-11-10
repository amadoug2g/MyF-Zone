package com.myfzone_sport.myf_zone.usecases.user

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.coach.ClubAffiliation

class GetUserClubUseCase(val repository: Repository) {
    operator fun invoke(clubAffiliation: ClubAffiliation) = repository.getUserClub(clubAffiliation)
}