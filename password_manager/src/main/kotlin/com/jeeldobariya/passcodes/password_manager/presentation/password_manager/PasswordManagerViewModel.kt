package com.jeeldobariya.passcodes.password_manager.presentation.password_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.password_manager.domain.usecases.RetrieveAllPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordManagerViewModel(
    var retrieveAllPasswordUseCase: RetrieveAllPasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordManagerState())
    val state = _state.asStateFlow()

    fun onAction(action: PasswordManagerAction) {
        when (action) {
            PasswordManagerAction.RefreshPassword -> {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            _state.update {
                PasswordManagerState(
                    passwordEntityList = retrieveAllPasswordUseCase()
                )
            }
        }
    }
}
