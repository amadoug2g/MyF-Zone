package com.myfzone_sport.myf_zone.usecases.affiliation

import com.myfzone_sport.myf_zone.data.Repository

class GetClubIdUseCase (val repository: Repository) {
    operator fun invoke(clubId: String) = repository.getClubFromId(clubId)
}