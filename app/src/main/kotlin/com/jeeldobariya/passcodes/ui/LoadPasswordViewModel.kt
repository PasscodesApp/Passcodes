package com.jeeldobariya.passcodes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.database.Password
import com.jeeldobariya.passcodes.utils.Controller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoadPasswordViewModel(
    var controller: Controller
): ViewModel() {

    private val _passwordsListState = MutableStateFlow(emptyList<Password>())
    val passwordsListState = _passwordsListState.asStateFlow()

    private val _isErrorState = MutableStateFlow(false)
    val isErrorState = _isErrorState.asStateFlow()

    fun loadInitialData() {
        viewModelScope.launch {
            _passwordsListState.update {
                controller.getAllPasswords().catch {
                    _isErrorState.update {
                        true
                    }
                }.first()
            }
        }
    }
}
