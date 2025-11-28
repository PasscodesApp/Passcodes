package com.jeeldobariya.passcodes.presentation.load_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.domain.usecases.RetrieveAllPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoadPasswordViewModel(
    var retrieveAllPasswordUseCase: RetrieveAllPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoadPasswordState())
    val state = _state.asStateFlow()

    fun onAction(action: LoadPasswordAction) {
        when (action) {
            LoadPasswordAction.RefreshPassword -> { refreshData() }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _state.update {
                LoadPasswordState(
                    passwordEntityList = retrieveAllPasswordUseCase.run()
                )
            }
        }
    }
}
