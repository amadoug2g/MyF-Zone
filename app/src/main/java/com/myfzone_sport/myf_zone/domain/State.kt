package com.myfzone_sport.myf_zone.domain

/**
 * Created by Amadou on 02/12/2020, 00:59
 *
 * State Class
 * : keeps track of the query status
 *
 */

sealed class State<T> {
    class Loading<T> : State<T>()
    data class Success<T>(val data: T) : State<T>()
    data class Failed<T>(val message: String) : State<T>()

    companion object {
        fun <T> loading() = Loading<T>()
        fun <T> success(data: T) = Success(data)
        fun <T> failed(message: String) = Failed<T>(message)
    }
}
