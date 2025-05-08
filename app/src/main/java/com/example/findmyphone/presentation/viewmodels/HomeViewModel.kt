package com.example.findmyphone.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(

) : ViewModel() {

    private val state =
        MutableStateFlow<AllUnInstallAppFragmentState>(AllUnInstallAppFragmentState.Init)
    val mState: StateFlow<AllUnInstallAppFragmentState> get() = state

}

sealed class AllUnInstallAppFragmentState {
    data object Init : AllUnInstallAppFragmentState()
    data class IsLoading(val isLoading: Boolean) : AllUnInstallAppFragmentState()
    data class ShowToast(val message: String) : AllUnInstallAppFragmentState()
}