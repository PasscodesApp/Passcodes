package com.jeeldobariya.passcodes.ui

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
): ViewModel() {
    var passwordEntityId: Int = -1

    private val _domainState = MutableStateFlow("")
    val domainState = _domainState.asStateFlow()

    private val _usernameState = MutableStateFlow("")
    val usernameState = _usernameState.asStateFlow()

    private val _passwordState = MutableStateFlow("")
    val passwordState = _passwordState.asStateFlow()

    private val _notesState = MutableStateFlow("")
    val notesState = _notesState.asStateFlow()

    private val _lastUpdatedAtState = MutableStateFlow("")
    val lastUpdatedAtState = _lastUpdatedAtState.asStateFlow()

    private val _isErrorState = MutableStateFlow(false)
    val isErrorState = _isErrorState.asStateFlow()

    fun loadInitialData(passwordId: Int) {
        passwordEntityId = passwordId

        viewModelScope.launch {
            try {
                val password: Password = controller.getPasswordById(passwordId)

                _domainState.update { password.domain }
                _usernameState.update { password.username }
                _passwordState.update { password.password }
                _notesState.update { password.notes }
                _lastUpdatedAtState.update {
                    DateTimeUtils.getRelativeDays(password.updatedAt.orEmpty())
                }
            } catch (_: Exception) {
                _isErrorState.update {
                    true
                }
            }
        }
    }

    fun onDeletePasswordButtonClick() {
        viewModelScope.launch {
            try {
                val rowsDeleted = controller.deletePassword(passwordEntityId)
            } catch (_: Exception) {
                _isErrorState.update {
                    true
                }
            }
        }
    }
}
