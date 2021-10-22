package com.myfzone_sport.myf_zone.common

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

/**
 * Created by Amadou on 21/10/2021, 17:07
 */

inline fun <reified T> mock() = Mockito.mock(T::class.java)
inline fun <T> whenever(methodCall: T) : OngoingStubbing<T> = Mockito.`when`(methodCall)