package com.myfzone_sport.myf_zone.usecases.user

import com.myfzone_sport.myf_zone.data.Repository

class GetUserUseCase (val repository: Repository) {
    operator fun invoke() = repository.getUserInfo()
}