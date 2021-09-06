package com.myfzone_sport.myf_zone.usecases.registration

import com.myfzone_sport.myf_zone.data.Repository

class AssignProfileImageUseCase(private val repository: Repository) {
    operator fun invoke() = repository.assignProfileImage()
}