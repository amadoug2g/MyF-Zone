package com.myfzone_sport.myf_zone.usecases.registration

import com.myfzone_sport.myf_zone.data.Repository

class SignInUserUseCase(private val repository: Repository) {
    operator fun invoke(email: String, password: String) = repository.signInUser(email, password)
}