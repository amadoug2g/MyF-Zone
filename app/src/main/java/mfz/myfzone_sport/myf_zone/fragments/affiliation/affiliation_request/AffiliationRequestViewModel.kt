package mfz.myfzone_sport.myf_zone.fragments.affiliation.affiliation_request

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import mfz.myfzone_sport.myf_zone.model.club.Club
import mfz.myfzone_sport.myf_zone.model.sport.Category
import mfz.myfzone_sport.myf_zone.model.sport.Sport

/**
 * Created by Amadou on 06/12/2020, 20:55
 *
 * Affiliation Request ViewModel class
 *
 */

class AffiliationRequestViewModel : ViewModel() {
    private val _clubList = MutableLiveData<MutableList<Club>>(mutableListOf())
    val clubList: LiveData<MutableList<Club>>
        get() = _clubList

    fun getCurrentUser() = AffiliationRequestService.getCurrentUser()

    //region Affiliation
    fun affiliationProcess(
        code: String,
        affiliationSport: String,
        affiliationCategory: String?,
        affiliationSubCategory: String?
    ) = AffiliationRequestService.affiliationProcess(
        code,
        affiliationSport,
        affiliationCategory,
        affiliationSubCategory
    )

    fun checkCode(code: String, clubList: MutableList<Club>) =
        AffiliationRequestService.checkCode(code, clubList)
    //endregion

    //region Sport
    suspend fun querySportList() = AffiliationRequestService.querySportList()
    fun querySportIdFromList(sport: String, list: MutableList<Sport>) =
        AffiliationRequestService.querySportIdFromList(sport, list)
    //endregion

    //region Club
    fun getClub() = AffiliationRequestService.getClub()
    fun assignClubList(list: MutableList<Club>) {
        _clubList.value = list
    }
    //endregion

    //region Category
    suspend fun queryCategoryList(sportId: String) =
        AffiliationRequestService.queryCategoryList(sportId)

    fun queryCategoryIdFromList(category: String, list: MutableList<Category>) =
        AffiliationRequestService.queryCategoryIdFromList(category, list)
    //endregion

    //region Sub-Category
    suspend fun querySubCategoryList(
        sportId: String,
        categoryId: String
    ) = AffiliationRequestService.querySubCategoryList(sportId, categoryId)
    //endregion

}