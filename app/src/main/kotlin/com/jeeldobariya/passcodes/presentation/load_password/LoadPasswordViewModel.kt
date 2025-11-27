package com.jeeldobariya.passcodes.presentation.load_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.data.repository.PasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoadPasswordViewModel(
    var passwordRepository: PasswordRepository
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
                    passwordEntityList = passwordRepository.getAllPasswords().catch {
                        _state.update { it.copy(isError = true) }
                    }.first()
                )
            }
        }
    }
}