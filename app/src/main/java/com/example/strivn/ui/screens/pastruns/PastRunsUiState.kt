package com.example.strivn.ui.screens.pastruns

import com.example.strivn.data.models.RunLog

data class PastRunsUiState(
    val isLoading: Boolean = false,
    val runs: List<RunLog> = emptyList(),
    val loadError: String? = null,
)
