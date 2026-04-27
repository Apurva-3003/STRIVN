package com.example.strivn.ui.screens.pastruns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.strivn.data.repository.RunHistoryStore
import com.example.strivn.data.repository.TrainingRepository
import com.example.strivn.data.repository.TrainingRepositoryProvider
import com.example.strivn.network.toUserVisibleMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVVM: [PastRunsUiState] from [RunHistoryStore.runsFlow], hydrated on launch via [TrainingRepository].
 */
class PastRunsViewModel : ViewModel() {

    private val trainingRepository: TrainingRepository = TrainingRepositoryProvider.instance

    private val _state = MutableStateFlow(PastRunsUiState())
    val state: StateFlow<PastRunsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadError = null) }
            val runsResult = trainingRepository.fetchRuns()
            runsResult.exceptionOrNull()?.let { e ->
                _state.update { it.copy(loadError = e.toUserVisibleMessage("Couldn’t load runs.")) }
            }
            _state.update { it.copy(isLoading = false) }
        }
        RunHistoryStore.runsFlow
            .onEach { runs ->
                _state.update { prev ->
                    prev.copy(runs = runs.sortedByDescending { it.date })
                }
            }
            .launchIn(viewModelScope)
    }

    fun dismissLoadError() {
        _state.update { it.copy(loadError = null) }
    }
}
