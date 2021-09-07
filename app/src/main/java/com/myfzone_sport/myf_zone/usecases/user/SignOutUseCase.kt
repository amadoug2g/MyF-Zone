package com.myfzone_sport.myf_zone.usecases.user

import com.myfzone_sport.myf_zone.data.Repository

/**
 * Created by Amadou on 07/09/2021, 19:34
 */

class SignOutUseCase (val repository: Repository) {
    operator fun invoke() = repository.signOut()
}