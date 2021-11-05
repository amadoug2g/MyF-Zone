package com.myfzone_sport.myf_zone.usecases.affiliation

import com.myfzone_sport.myf_zone.data.Repository

class GetSportListUseCase (val repository: Repository) {
    operator fun invoke() = repository.getSportList()
}