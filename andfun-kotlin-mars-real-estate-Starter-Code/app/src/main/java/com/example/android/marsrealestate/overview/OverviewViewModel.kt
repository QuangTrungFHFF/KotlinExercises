/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.marsrealestate.overview

import android.util.Log
import androidx.lifecycle.*
import com.example.android.marsrealestate.network.MarsApi
import com.example.android.marsrealestate.network.MarsApiFilter
import com.example.android.marsrealestate.network.MarsProperty
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


enum class MarsApiStatus { LOADING, ERROR, DONE }
/**
 * The [ViewModel] that is attached to the [OverviewFragment].
 */
class OverviewViewModel : ViewModel() {


    private val _navigateToDetail = MutableLiveData<MarsProperty>()
    val navigateToDetail : LiveData<MarsProperty>
        get() = _navigateToDetail

    // The internal MutableLiveData String that stores the status of the most recent request
    private val _status = MutableLiveData<MarsApiStatus>()

    // The external immutable LiveData for the request status String
    val status: LiveData<MarsApiStatus>
        get() = _status

    private val _property = MutableLiveData<List<MarsProperty>>()
    val property : LiveData<List<MarsProperty>>
            get() = _property



    /**
     * Call getMarsRealEstateProperties() on init so we can display status immediately.
     */
    init {
        getMarsRealEstateProperties(MarsApiFilter.SHOW_ALL)
    }

    /**
     * Sets the value of the status LiveData to the Mars API status.
     */
    private fun getMarsRealEstateProperties(filter : MarsApiFilter) {
        viewModelScope.launch {
            try {
                _status.value = MarsApiStatus.LOADING
                val listResult = MarsApi.retrofitService.getProperties(filter.value)
                _status.value = MarsApiStatus.DONE
                if(listResult.size>1){
                    _property.value = listResult
                }
            }catch (e :Exception){
                _status.value = MarsApiStatus.ERROR
                _property.value = ArrayList()
            }
        }
    }

    fun displayItemDetail(marsProperty: MarsProperty){
        _navigateToDetail.value = marsProperty
    }

    fun completeNavigateToDetail(){
        _navigateToDetail.value = null
    }

    fun updateFilter(filter: MarsApiFilter){
        getMarsRealEstateProperties(filter)
    }
}
