package com.jeeldobariya.passcodes.password_manager.presentation.view_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.password_manager.domain.modals.PasswordModal
import com.jeeldobariya.passcodes.password_manager.domain.usecases.DeletePasswordUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.RetrievePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewPasswordViewModel(
    val retrievePasswordUseCase: RetrievePasswordUseCase,
    val deletePasswordUseCase: DeletePasswordUseCase
) : ViewModel() {
    var passwordEntityId: Int = -1

    private val _state = MutableStateFlow(ViewPasswordState())
    val state = _state.asStateFlow()

    fun onAction(action: ViewPasswordAction) {
        when (action) {
            is ViewPasswordAction.LoadPassword -> { refreshData(action.passwordId) }
            ViewPasswordAction.RefreshPassword -> { refreshData(passwordEntityId) }
            ViewPasswordAction.DeletePasswordAction -> { deletePasswordEntity() }
        }
    }

    private fun refreshData(passwordId: Int) {
        passwordEntityId = passwordId

        viewModelScope.launch {
            try {
                val password: PasswordModal = requireNotNull(retrievePasswordUseCase(passwordId))

                _state.update {
                    it.copy(
                        domain = password.domain,
                        username = password.username,
                        password = password.password,
                        notes = password.notes,
                        lastUpdatedAt = password.lastUpdatedAt
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(isError = true) }
            }
        }
    }

    private fun deletePasswordEntity() {
        viewModelScope.launch {
            try {
                deletePasswordUseCase(passwordEntityId)
            } catch (_: Exception) {
                _state.update { it.copy(isError = true) }
            }
        }
    }
}
