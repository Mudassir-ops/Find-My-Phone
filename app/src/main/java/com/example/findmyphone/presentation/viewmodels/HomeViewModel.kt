package com.example.findmyphone.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.findmyphone.data.other.DetectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val detectionRepository: DetectionRepository
) : ViewModel() {

    val serviceState: StateFlow<Boolean> = detectionRepository.isServiceRunningStateFlow


}

sealed class AllUnInstallAppFragmentState {
    data object Init : AllUnInstallAppFragmentState()
    data class IsLoading(val isLoading: Boolean) : AllUnInstallAppFragmentState()
    data class ShowToast(val message: String) : AllUnInstallAppFragmentState()
}