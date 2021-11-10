package com.myfzone_sport.myf_zone.usecases.user

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.coach.Coach

class GetUserClubAffiliationUseCase(val repository: Repository) {
    operator fun invoke(coach: Coach) = repository.getUserAffiliation(coach)
}