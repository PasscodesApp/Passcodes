package com.jeeldobariya.passcodes.oldui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.utils.Controller
import com.jeeldobariya.passcodes.utils.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ViewPasswordViewModel(
    val controller: Controller
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
                val password: Password = controller.getPasswordById(passwordId)

                _state.update {
                    it.copy(
                        domain = password.domain,
                        username = password.username,
                        password = password.password,
                        notes = password.notes,
                        lastUpdatedAt = DateTimeUtils.getRelativeDays(password.updatedAt.orEmpty())
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
                controller.deletePassword(passwordEntityId)
            } catch (_: Exception) {
                _state.update { it.copy(isError = true) }
            }
        }
    }
}
