package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.Apartment
import portaria.project.data.repository.ApartmentRepository

data class ApartamentoFormFields(
    val number: String = "",
    val block: String = "",
    val floor: String = "",
    val parkingSpotCount: Int = 0,
    val notes: String = "",
    val isActive: Boolean = true
)

data class ApartamentoUiState(
    val apartamentos: List<Apartment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val form: ApartamentoFormFields = ApartamentoFormFields()
)

class ApartamentoViewModel(
    private val repository: ApartmentRepository = ApartmentRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApartamentoUiState())
    val uiState: StateFlow<ApartamentoUiState> = _uiState

    fun loadApartments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getAll()
                .onSuccess { apts -> _uiState.value = _uiState.value.copy(apartamentos = apts, isLoading = false, error = null) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao carregar apartamentos") }
        }
    }

    fun setApartamentoForm(update: (ApartamentoFormFields) -> ApartamentoFormFields) {
        _uiState.value = _uiState.value.copy(form = update(_uiState.value.form))
    }

    fun prepareApartamentoForm(id: String) {
        if (id.isBlank()) {
            _uiState.value = _uiState.value.copy(form = ApartamentoFormFields())
            return
        }
        val apt = _uiState.value.apartamentos.find { it.id == id }
        if (apt != null) {
            _uiState.value = _uiState.value.copy(
                form = ApartamentoFormFields(
                    number = apt.number,
                    block = apt.block,
                    floor = apt.floor,
                    parkingSpotCount = apt.parkingSpotCount,
                    notes = apt.notes,
                    isActive = apt.isActive
                )
            )
        }
    }

    fun submitApartment(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val f = _uiState.value.form

            val apt = Apartment(
                id = if (id.isNotBlank()) id else null,
                number = f.number,
                block = f.block,
                floor = f.floor,
                parkingSpotCount = f.parkingSpotCount,
                notes = f.notes,
                isActive = f.isActive
            )

            val result = if (id.isNotBlank()) repository.update(id, apt) else repository.create(apt)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Salvo com sucesso")
                loadApartments()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Erro ao salvar")
            }
        }
    }

    fun deleteApartment(id: String) {
        viewModelScope.launch {
            repository.delete(id).onSuccess { loadApartments() }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}