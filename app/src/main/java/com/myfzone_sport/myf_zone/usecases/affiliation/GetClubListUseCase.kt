package com.myfzone_sport.myf_zone.usecases.affiliation

import com.myfzone_sport.myf_zone.data.Repository

class GetClubListUseCase (val repository: Repository) {
    operator fun invoke() = repository.getClubList()
}