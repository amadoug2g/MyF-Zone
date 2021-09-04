package com.myfzone_sport.myf_zone.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FragmentViewModel(private val getAllEventsUseCase: GetAllEventsUseCase) : ViewModel() {

    //region Variables
    private val _closeEventsList = MutableLiveData<MutableList<Event>>()
    val closeEventsList = _closeEventsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    //endregion

    //region Functions
//    init {
//        try {
//            getCloseEvents()
//        } catch (e: Exception) {
//            Log.e("getCloseEvents", "Error: $e")
//        }
//    }

    fun getCloseEvents() {
        viewModelScope.launch(IO) {
            getAllEventsUseCase.invoke().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startLoading()
                        Log.i("getCloseEvents", "Loading")
                    }
                    is State.Success -> {
                        onResult()
                        _closeEventsList.postValue(state.data)
//                        Log.i("getCloseEvents", "Data: ${state.data}")
                        state.data.forEach {
                            Log.i("getCloseEvents", "Data: $it")
                        }
                    }
                    is State.Failed -> {
                        Log.i("getCloseEvents", "Error: ${state.message}")
                        val message = "Error while fetching close events: ${state.message}"
                        onResult(message)
                    }
                }
            }
        }
    }
    //endregion

    //region Observers
    private fun onResult(message: String = "") {
        _errorMessage.postValue(message)
        stopLoading()
    }

    private fun startLoading() {
        _isLoading.postValue(true)
    }

    private fun stopLoading() {
        _isLoading.postValue(false)
    }
    //endregion
}

class FragmentViewModelFactory(private val getAllEventsUseCase: GetAllEventsUseCase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(GetAllEventsUseCase::class.java)
            .newInstance(getAllEventsUseCase)
    }

}