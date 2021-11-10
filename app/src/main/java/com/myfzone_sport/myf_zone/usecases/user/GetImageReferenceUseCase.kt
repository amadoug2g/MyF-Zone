package com.myfzone_sport.myf_zone.usecases.user

import com.myfzone_sport.myf_zone.app.framework.FirebaseService
import com.myfzone_sport.myf_zone.data.Repository

class GetImageReferenceUseCase (val repository: Repository) {
    operator fun invoke(path: String) = repository.getImageReference(path)
}