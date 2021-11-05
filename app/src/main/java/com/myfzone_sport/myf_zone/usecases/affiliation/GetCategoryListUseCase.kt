package com.myfzone_sport.myf_zone.usecases.affiliation

import com.myfzone_sport.myf_zone.data.Repository

class GetCategoryListUseCase (val repository: Repository) {
    operator fun invoke(sportId: String) = repository.getCategoryList(sportId)
}