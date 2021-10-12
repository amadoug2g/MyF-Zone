package com.myfzone_sport.myf_zone.usecases.user

import com.myfzone_sport.myf_zone.data.Repository

class GetUserClubUseCase(val repository: Repository) {
    operator fun invoke() = repository.getUserClub()
}