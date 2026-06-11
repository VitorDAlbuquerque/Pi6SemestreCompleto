package portaria.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import portaria.project.data.models.Vehicle
import portaria.project.data.models.User
import portaria.project.data.repository.VehicleRepository

data class VeiculoFormFields(
    val placa: String = "",
    val marca: String = "",
    val modelo: String = "",
    val cor: String = "",
    val ano: String = "",
    val tipo: String = "CARRO",
    val notas: String = "",
    val ativo: Boolean = true,
    val selectedApartmentId: String? = null
)

data class VeiculoUiState(
    val isLoading: Boolean = false,
    val veiculos: List<Vehicle> = emptyList(),
    val veiculoSelecionado: Vehicle? = null,
    val form: VeiculoFormFields = VeiculoFormFields(),
    val error: String? = null,
    val successMessage: String? = null
)

class VeiculoViewModel(
    private val repository: VehicleRepository = VehicleRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VeiculoUiState())
    val uiState: StateFlow<VeiculoUiState> = _uiState
    private var formLoadJob: Job? = null

    fun setVeiculoForm(transform: (VeiculoFormFields) -> VeiculoFormFields) {
        _uiState.value = _uiState.value.copy(form = transform(_uiState.value.form))
    }

    fun clearVeiculoForm() {
        _uiState.value = _uiState.value.copy(
            veiculoSelecionado = null,
            form = VeiculoFormFields(),
            error = null,
            successMessage = null
        )
    }

    fun prepareVeiculoForm(veiculoId: String, fixedApartmentId: String? = null) {
        if (veiculoId.isBlank()) {
            formLoadJob?.cancel()
            formLoadJob = null
            _uiState.value = _uiState.value.copy(
                veiculoSelecionado = null,
                form = VeiculoFormFields(
                    selectedApartmentId = fixedApartmentId?.takeIf { it.isNotBlank() }
                ),
                error = null,
                successMessage = null
            )
        } else {
            carregarVeiculo(veiculoId)
        }
    }

    fun loadVeiculos(authenticatedUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val role = authenticatedUser?.role?.uppercase()?.trim()
            val apartmentId = if (role == "MORADOR") authenticatedUser.apartmentId else null

            repository.getAll(apartmentId)
                .onSuccess { list ->
                    val filteredList = when (role) {
                        "MORADOR" -> list.filter { it.apartmentId == authenticatedUser.apartmentId }
                        "ADMIN", "PORTEIRO", "GERENTE" -> list
                        else -> emptyList()
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, veiculos = filteredList)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar veículos")
                }
        }
    }

    private fun buildVehicleFromForm(veiculoId: String, fixedApartmentId: String?): Vehicle {
        val f = _uiState.value.form
        val apartmentId = fixedApartmentId?.takeIf { it.isNotBlank() }
            ?: f.selectedApartmentId?.takeIf { it.isNotBlank() }
            ?: ""
        val ex = _uiState.value.veiculoSelecionado
        return Vehicle(
            id = veiculoId.ifBlank { null },
            plate = f.placa,
            brand = f.marca,
            model = f.modelo,
            color = f.cor,
            year = f.ano.toIntOrNull(),
            type = f.tipo,
            apartmentId = apartmentId,
            visitorId = ex?.visitorId ?: "",
            notes = f.notas,
            isActive = f.ativo
        )
    }

    fun submitVeiculo(veiculoId: String, fixedApartmentId: String? = null, currentUser: User? = null) {
        val resolvedApartmentId = if (currentUser?.role?.uppercase()?.trim() == "MORADOR") {
            currentUser.apartmentId
        } else {
            fixedApartmentId
        }
        val vehicle = buildVehicleFromForm(veiculoId, resolvedApartmentId)
        if (veiculoId.isBlank()) {
            createVeiculo(vehicle, currentUser)
        } else {
            updateVeiculo(veiculoId, vehicle.copy(id = veiculoId), currentUser)
        }
    }

    fun createVeiculo(vehicle: Vehicle, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.create(vehicle)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Veículo cadastrado com sucesso")
                    loadVeiculos(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao cadastrar veículo")
                }
        }
    }

    fun updateVeiculo(id: String, vehicle: Vehicle, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.update(id, vehicle)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Veículo atualizado")
                    loadVeiculos(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao atualizar veículo")
                }
        }
    }

    fun deleteVeiculo(id: String, currentUser: User?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.delete(id)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Veículo removido")
                    loadVeiculos(currentUser)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao remover veículo")
                }
        }
    }

    fun carregarVeiculo(id: String) {
        formLoadJob?.cancel()
        formLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getById(id)
                .onSuccess { v ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        veiculoSelecionado = v,
                        form = VeiculoFormFields(
                            placa = v.plate,
                            marca = v.brand,
                            modelo = v.model,
                            cor = v.color,
                            ano = v.year?.toString() ?: "",
                            tipo = v.type.ifBlank { "CARRO" },
                            notas = v.notes,
                            ativo = v.isActive,
                            selectedApartmentId = v.apartmentId.takeIf { it.isNotBlank() }
                        )
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Erro ao carregar veículo")
                }
        }
    }

    fun clearVeiculoSelecionado() {
        _uiState.value = _uiState.value.copy(veiculoSelecionado = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}