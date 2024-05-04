package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.SingleLiveEvent
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.database.Result
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.repository.ElectionRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class ElectionsViewModel(application: Application) : ViewModel() {
    private val dao = ElectionDatabase.getInstance(application).electionDao
    private val repository = ElectionRepository(dao)

    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    private val _navigateVoterInfo = MutableLiveData<Election?>()
    val navigateVoterInfo: LiveData<Election?>
        get() = _navigateVoterInfo

    init {
        getElections()
    }

    private fun getElections() {
        showLoading.value = true
        viewModelScope.launch {
            try {
                val result = CivicsApi.retrofitService.getElections()
                Timber.d(result.toString())
                showLoading.postValue(false)

                _upcomingElections.value = result.elections
            } catch (e: Exception) {
                Timber.e("Failure: " + e.message)
            }
        }
    }

    fun getSavedElection() {
        viewModelScope.launch {
            when (val result = repository.getAllElection()) {
                is Result.Success<List<Election>> -> {
                    val elections = result.data
                    _savedElections.value = elections
                }

                is Result.Error -> Timber.e(result.message)
            }
            showLoading.postValue(false)
        }
    }

    fun onElectionClicked(election: Election) {
        _navigateVoterInfo.value = election
    }

    fun onNavigated() {
        _navigateVoterInfo.value = null
    }
}