package com.jeeldobariya.passcodes.ui

sealed interface LoadPasswordAction {
    data object refreshPassswordData: LoadPasswordAction
}
