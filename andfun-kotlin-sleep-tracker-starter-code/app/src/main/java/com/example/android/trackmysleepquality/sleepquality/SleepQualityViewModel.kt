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
 */

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabase
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

class SleepQualityViewModel(
        private val nightIdKey : Long = 0L,
        val database: SleepDatabaseDao
) : ViewModel(){

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _navigationToSleepTracker = MutableLiveData<Boolean?>()
    val navigationToSleepTracker : LiveData<Boolean?>
        get() = _navigationToSleepTracker

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun onCompleteNavigating(){
        _navigationToSleepTracker.value = false
    }

    fun onSetSleepQuality(quality : Int){
        uiScope.launch {
            setSleepQuality(quality)
        }
    }

    private suspend fun setSleepQuality(quality: Int) {
        withContext(Dispatchers.IO){
            val night : SleepNight = database.get(nightIdKey) ?: return@withContext
            night.sleepQuality = quality
            database.update(night)
        }
    }


}
