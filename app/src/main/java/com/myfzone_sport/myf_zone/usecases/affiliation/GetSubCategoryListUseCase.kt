package com.myfzone_sport.myf_zone.usecases.affiliation

import com.myfzone_sport.myf_zone.data.Repository

class GetSubCategoryListUseCase (val repository: Repository) {
    operator fun invoke(sportId: String, categoryId: String) = repository.getSubCategoryList(sportId, categoryId)
}