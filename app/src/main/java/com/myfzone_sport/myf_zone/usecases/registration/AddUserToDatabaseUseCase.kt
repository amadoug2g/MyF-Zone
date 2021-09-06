package com.myfzone_sport.myf_zone.usecases.registration

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.coach.Coach

class AddUserToDatabaseUseCase(private val repository: Repository) {
    operator fun invoke(coach: Coach) = repository.addUserToDatabase(coach)

}