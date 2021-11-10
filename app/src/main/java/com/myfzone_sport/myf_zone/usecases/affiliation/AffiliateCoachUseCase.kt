package com.myfzone_sport.myf_zone.usecases.affiliation

import com.myfzone_sport.myf_zone.data.Repository
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.sport.Category
import com.myfzone_sport.myf_zone.domain.sport.Sport
import com.myfzone_sport.myf_zone.domain.sport.SubCategory

class AffiliateCoachUseCase (val repository: Repository) {
    operator fun invoke(club: Club, sport: Sport, category: Category?, subCategory: SubCategory?) = repository.affiliateCoach(club, sport, category, subCategory)
}