package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.club.Club
import com.myfzone_sport.myf_zone.domain.sport.Category
import com.myfzone_sport.myf_zone.domain.sport.Sport
import com.myfzone_sport.myf_zone.domain.sport.SubCategory
import com.myfzone_sport.myf_zone.usecases.affiliation.GetCategoryListUseCase
import com.myfzone_sport.myf_zone.usecases.affiliation.GetClubListUseCase
import com.myfzone_sport.myf_zone.usecases.affiliation.GetSportListUseCase
import com.myfzone_sport.myf_zone.usecases.affiliation.GetSubCategoryListUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Amadou on 03/11/2021, 12:28
 */

class AffiliationRequestListViewModel(
//    private val affiliateCoachUseCase: AffiliateCoachUseCase,
    private val getSportListUseCase: GetSportListUseCase,
    private val getClubListUseCase: GetClubListUseCase,
    private val getCategoryListUseCase: GetCategoryListUseCase,
    private val getSubCategoryListUseCase: GetSubCategoryListUseCase
) : ViewModel() {

    //region Variables
    private val _clubList = MutableLiveData<MutableList<Club>>()
    val clubList: LiveData<MutableList<Club>> = _clubList

    private val _club = MutableLiveData<Club>()
    val club: LiveData<Club> = _club

    private val _sportList = MutableLiveData<MutableList<Sport>>()
    val sportList: LiveData<MutableList<Sport>> = _sportList

    private val _sport = MutableLiveData<Sport>()
    val sport: LiveData<Sport> = _sport

    private val _categoryList = MutableLiveData<MutableList<Category>>()
    val categoryList: LiveData<MutableList<Category>> = _categoryList

    private val _category = MutableLiveData<Category>()
    val categorySelected: LiveData<Category> = _category

    private val _subCategoryList = MutableLiveData<MutableList<SubCategory>>()
    val subCategoryList: LiveData<MutableList<SubCategory>> = _subCategoryList

    private val _subCategory = MutableLiveData<SubCategory>()
    val subCategory: LiveData<SubCategory> = _subCategory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
    init {
        getClubList()
        getSportList()
    }

    private fun getClubList() {
        viewModelScope.launch {
            getClubListUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val clubList = state.data

                        if (clubList.isNotEmpty()) {
                            _clubList.postValue(clubList)

                            onResult()
                        } else {
                            onResult("Club list is empty")
                        }
                    }
                    is State.Failed -> {
                        val message = "Club fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getClubItem(club: String, clubList: MutableList<Club>) {
        for (i in clubList)
            if (i.name == club) _club.postValue(i)
    }

    fun assignClub(club: Club) {
        _club.postValue(club)
    }

    private fun getSportList() {
        viewModelScope.launch {
            getSportListUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val sportList = state.data

                        if (sportList.isNotEmpty()) {
                            _sportList.postValue(sportList)

                            onResult()
                        } else {
                            onResult("Sport list is empty")
                        }
                    }
                    is State.Failed -> {
                        val message = "Sport fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getSportItem(sport: String, sportList: MutableList<Sport>) {
        for (i in sportList)
            if (i.name == sport) {
                _sport.postValue(i)
                getCategoryList(i.id)
            }
    }

    fun assignSport(sport: Sport) {
        _sport.postValue(sport)
    }

    private fun getCategoryList(sportId: String) {
        viewModelScope.launch {
            getCategoryListUseCase.invoke(sportId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val categoryList = state.data

                        if (categoryList.isNotEmpty()) {
                            _categoryList.postValue(categoryList)

                            onResult()
                        } else {
                            onResult("Category list is empty")
                        }
                    }
                    is State.Failed -> {
                        val message = "Category fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getCategoryItem(category: String, categoryList: MutableList<Category>) {
        for (i in categoryList)
            if (i.name == category) {
                _category.postValue(i)
//                getSubCategoryList(_sportSelected.value!!.id, i.id)
            }
    }


    fun assignCategory(category: Category) {
        _category.postValue(category)
    }

    private fun getSubCategoryList(sportId: String, categoryId: String) {
        viewModelScope.launch {
            getSubCategoryListUseCase.invoke(sportId, categoryId).collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                    }
                    is State.Success -> {
                        val subCategoryList = state.data

                        if (subCategoryList.isNotEmpty()) {
                            _subCategoryList.postValue(subCategoryList)

                            onResult()
                        } else {
                            onResult("Sub-category list is empty")
                        }
                    }
                    is State.Failed -> {
                        val message = "Sub-category fetching failed: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }

    fun getSubCategoryItem(subCategory: String, subCategoryList: MutableList<SubCategory>) {
        Log.d("tagging", "select sub cat 03")
        for (i in subCategoryList)
            if (i.name == subCategory) _subCategory.postValue(i)
    }

    fun assignSubCategory(subCategory: SubCategory) {
        _subCategory.postValue(subCategory)
    }
    //endregion

    //region Observers
    private fun onResult(message: String = "") {
        _errorMessage.postValue(message)
        stopLoading()
        resetErrorMsg()
    }

    private fun resetErrorMsg() {
        _errorMessage.postValue("")
    }

    private fun startLoading() {
        _isLoading.postValue(true)
    }

    private fun stopLoading() {
        _isLoading.postValue(false)
    }
    //endregion
}

class AffiliationRequestListViewModelFactory(
    private val getSportListUseCase: GetSportListUseCase,
    private val getClubListUseCase: GetClubListUseCase,
    private val getCategoryListUseCase: GetCategoryListUseCase,
    private val getSubCategoryListUseCase: GetSubCategoryListUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(
            GetSportListUseCase::class.java,
            GetClubListUseCase::class.java,
            GetCategoryListUseCase::class.java,
            GetSubCategoryListUseCase::class.java,
        )
            .newInstance(
                getSportListUseCase,
                getClubListUseCase,
                getCategoryListUseCase,
                getSubCategoryListUseCase,
            )
    }

}