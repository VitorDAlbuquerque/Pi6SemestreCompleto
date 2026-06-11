package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.RegistroAcesso
import portaria.project.data.repository.HistoricoRepository

data class HistoricoUiState(
    val isLoading: Boolean = false,
    val registros: List<RegistroAcesso> = emptyList(),
    val error: String? = null
)

class HistoricoViewModel(
    private val repository: HistoricoRepository = HistoricoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoricoUiState())
    val uiState: StateFlow<HistoricoUiState> = _uiState


    fun loadHistorico() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getHistorico()
                .onSuccess { list -> _uiState.value = _uiState.value.copy(isLoading = false, registros = list) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar histórico") }
        }
    }
}