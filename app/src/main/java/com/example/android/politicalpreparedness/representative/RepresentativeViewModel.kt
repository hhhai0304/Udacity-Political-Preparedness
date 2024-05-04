package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch
import timber.log.Timber

class RepresentativeViewModel(state: SavedStateHandle) : ViewModel() {
    companion object {
        private const val ADDRESS_KEY = "address"
        private const val REPRESENTATIVE_KEY = "representative"
    }

    private val savedStateHandle = state

    private val _representatives =
        savedStateHandle.getLiveData<List<Representative>>(REPRESENTATIVE_KEY)
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    private val _addressInput = savedStateHandle.getLiveData(
        ADDRESS_KEY,
        initialValue = Address(line1 = "", city = "", state = "", zip = "")
    )
    val addressInput: LiveData<Address>
        get() = _addressInput

    fun getState(state: String) {
        updateAddress(
            _addressInput.value?.copy(state = state) ?: Address(
                line1 = "",
                city = "",
                state = "",
                zip = ""
            )
        )
    }

    fun updateAddress(newAddress: Address) {
//        _addressInput.value = newAddress
        savedStateHandle[ADDRESS_KEY] = newAddress
    }

    private fun updateRepresentatives(newRepresentatives: List<Representative>) {
//        _representatives.value = newRepresentatives
        savedStateHandle[REPRESENTATIVE_KEY] = newRepresentatives
    }

    fun getRepresentatives(address: Address) {
        viewModelScope.launch {
            try {
                val (offices, officials) =
                    CivicsApi.retrofitService.getRepresentatives(address = address.toFormattedString())
                Timber.d("ELECTIONS SUCCESS: $offices")
                updateRepresentatives(offices.flatMap { office ->
                    office.getRepresentatives(
                        officials
                    )
                })
            } catch (e: Exception) {
                Timber.e("Failure: ${e.message}")
            }
        }
    }
}
