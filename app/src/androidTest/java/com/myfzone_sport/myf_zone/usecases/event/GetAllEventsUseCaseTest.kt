package com.myfzone_sport.myf_zone.usecases.event

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.common.mock
import com.myfzone_sport.myf_zone.common.whenever
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock

/**
 * Created by Amadou on 21/10/2021, 16:49
 */

class GetAllEventsUseCaseTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    val remoteDataSource: RemoteDataSourceImpl = mock<RemoteDataSourceImpl>()
    val repository by lazy { RepositoryImpl(remoteDataSource) }
    val getAllEventsUseCase by lazy { GetAllEventsUseCase(repository) }

    @Mock
    private lateinit var flow: Flow<State<MutableList<Event>>>

    @Test
    fun repoText() = runBlocking {
        var result = mutableListOf<Event>()
        flow = getAllEventsUseCase.invoke()

//        assertThat
        assertThat(flow.collect { state ->

        })
//            .collect { state ->
//            when (state) {
//                is State.Loading -> {
//
//                }
//                is State.Success -> {
//                    result = state.data
//                }
//                is State.Failed -> {
//
//                }
//            }
//        }


    }

    @Test
    fun testAllEventsSuccess() {

    }


    fun testAllEventsFailure() {

    }
}