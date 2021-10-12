package com.myfzone_sport.myf_zone.usecases.user

import com.myfzone_sport.myf_zone.data.Repository

/**
 * Created by Amadou on 10/10/2021, 18:11
 */

class GetUserStatusUseCase(val repository: Repository) {
    operator fun invoke() = repository.getUserStatus()
}