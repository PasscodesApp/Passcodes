package com.jeeldobariya.passcodes.password_manager.presentation.password_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeeldobariya.passcodes.password_manager.domain.usecases.ExportPasswordCSVUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.ImportPasswordCSVUseCase
import com.jeeldobariya.passcodes.password_manager.domain.usecases.RetrieveAllPasswordUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PasswordManagerViewModel(
    var retrieveAllPasswordUseCase: RetrieveAllPasswordUseCase,
    val importPasswordCSVUseCase: ImportPasswordCSVUseCase,
    val exportPasswordCSVUseCase: ExportPasswordCSVUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PasswordManagerState())
    val state = _state.asStateFlow()

    fun onAction(action: PasswordManagerAction) {
        when (action) {
            PasswordManagerAction.RefreshPassword -> {
                refreshData()
            }

            is PasswordManagerAction.OnImportGooglePassword -> {
                viewModelScope.launch(Dispatchers.IO) {
                    importPasswordCSVUseCase(action.fileUri)
                }
            }

            is PasswordManagerAction.OnExportGooglePassword -> {
                viewModelScope.launch(Dispatchers.IO) {
                    exportPasswordCSVUseCase(action.fileUri)
                }
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
