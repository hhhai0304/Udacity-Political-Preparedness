package com.example.android.politicalpreparedness.election

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.SingleLiveEvent
import com.example.android.politicalpreparedness.database.ElectionDatabase.Companion.getInstance
import com.example.android.politicalpreparedness.database.Result
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import com.example.android.politicalpreparedness.repository.ElectionRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class VoterInfoViewModel(application: Application) : ViewModel() {

    val showLoading: SingleLiveEvent<Boolean> = SingleLiveEvent()

    private val dao = getInstance(application).electionDao

    private val repository = ElectionRepository(dao)

    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address

    var election: Election? = null

    private val _saveButtonName = MutableLiveData<String>()
    val saveButtonName: LiveData<String>
        get() = _saveButtonName

    private val _electionData = MutableLiveData<Election?>()
    val electionData: LiveData<Election?>
        get() = _electionData

    fun getVoterInfo(id: Int, division: String) {
        showLoading.value = true
        viewModelScope.launch {
            try {
                val result =
                    CivicsApi.retrofitService.getVoterInfo(
                        division, id.toLong(),
                        productionDataOnly = true,
                        returnAllAvailableData = true
                    )
                showLoading.postValue(false)
                election = result.election
                _voterInfo.value = result
                if (result.state.isNullOrEmpty()) {
                    _address.value = ""
                } else {
                    _address.value =
                        result.state.first().electionAdministrationBody.correspondenceAddress?.line1
                            ?: ""
                }
                Timber.d("ELECTIONS SUCCESS: ${_voterInfo.value}")
            } catch (e: Exception) {
                Timber.e("Failure: " + e.message)
            }
        }
    }

    fun insertElectionToDatabase() {
        viewModelScope.launch {
            if (election != null) {
                repository.insertElection(election!!)
            }
        }
    }

    fun getElectionById(id: Int) {
        viewModelScope.launch {
            when (val result = repository.getElectionById(id)) {
                is Result.Success<Election> -> {
                    val electionData = result.data
                    _electionData.value = electionData
                    _saveButtonName.value = "UNFOLLOW ELECTION"
                }

                is Result.Error -> {
                    Timber.e(result.message)
                    _saveButtonName.value = "FOLLOW ELECTION"
                }
            }
        }
    }

    fun deleteElectionById(id: Int) {
        viewModelScope.launch {
            repository.deleteElectionById(id)
        }
    }
}