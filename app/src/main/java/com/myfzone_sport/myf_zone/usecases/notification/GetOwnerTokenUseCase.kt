package com.myfzone_sport.myf_zone.usecases.notification

import com.myfzone_sport.myf_zone.data.Repository

class GetOwnerTokenUseCase(val repository: Repository) {
    operator fun invoke(ownerId: String) = repository.getOwnerToken(ownerId)
}