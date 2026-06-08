package com.example.demo.service


import com.example.demo.model.Apartment
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.UserRepository
import com.example.demo.repository.VehicleRepository
import org.springframework.stereotype.Service

// Serviço e lógica de negócio para apartamentos (ApartmentService)
@Service
class ApartmentService(
    private val repository: ApartmentRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) {

    fun create(apartment: Apartment): String {
        normalizeApartment(apartment)
        validateApartment(apartment)

        if (repository.findByNumberAndBlock(apartment.number, apartment.block) != null) {
            throw IllegalArgumentException("Já existe um apartamento cadastrado com este número e bloco.")
        }

        validateResidents(apartment.residentIds)
        apartment.isOccupied = apartment.residentIds.isNotEmpty()
        val id = repository.save(apartment)
        syncVehicles(id, apartment.vehicleIds, emptyList())
        return id
    }

    fun getAll() = repository.findAll()

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Apartamento não encontrado.")

    fun update(id: String, apartment: Apartment) {
        val existingApartment = repository.findById(id)
            ?: throw NoSuchElementException("Apartamento não encontrado.")

        normalizeApartment(apartment)
        validateApartment(apartment)

        val sameUnit = apartment.number == existingApartment.number && apartment.block == existingApartment.block
        if (!sameUnit) {
            val apartmentsWithSameUnit = repository.findAllByNumberAndBlock(apartment.number, apartment.block)
            val unitAlreadyExists = apartmentsWithSameUnit.any { it.id != id }
            if (unitAlreadyExists) {
                throw IllegalArgumentException("Já existe um apartamento cadastrado com este número e bloco.")
            }
        }

        validateResidents(apartment.residentIds)
        apartment.isOccupied = apartment.residentIds.isNotEmpty()
        repository.update(id, apartment)
        syncVehicles(id, apartment.vehicleIds, existingApartment.vehicleIds)
    }

    fun delete(id: String) {
        repository.findById(id) ?: throw NoSuchElementException("Apartamento não encontrado.")
        repository.delete(id)
    }

    private fun validateApartment(apartment: Apartment) {
        if (apartment.number.isBlank()) {
            throw IllegalArgumentException("Número do apartamento e obrigatório.")
        }
        if (apartment.block.isBlank()) {
            throw IllegalArgumentException("Bloco e obrigatório.")
        }
        if (apartment.maxResidents < 0) {
            throw IllegalArgumentException("A capacidade máxima não pode ser negativa.")
        }
        if (apartment.maxResidents > 0 && apartment.residentIds.size > apartment.maxResidents) {
            throw IllegalArgumentException("A quantidade de moradores excede a capacidade máxima do apartamento.")
        }
        if (apartment.vehicleIds.size > apartment.parkingSpotCount) {
            throw IllegalArgumentException("A quantidade de veiculos não pode ser maior que o total de vagas.")
        }
        if (apartment.parkingSpotCount < 0) {
            throw IllegalArgumentException("A quantidade de vagas não pode ser negativa.")
        }
        if (apartment.availableParkingSpots < 0) {
            throw IllegalArgumentException("A quantidade de vagas disponíveis não pode ser negativa.")
        }
        if (apartment.availableParkingSpots > apartment.parkingSpotCount) {
            throw IllegalArgumentException("As vagas disponíveis não podem ser maiores que o total de vagas.")
        }
    }

    private fun validateResidents(residentIds: List<String>) {
        val ids = residentIds.map { it.trim() }.filter { it.isNotBlank() }
        if (ids.size != ids.distinct().size) {
            throw IllegalArgumentException("Não é permitido repetir moradores no apartamento.")
        }

        ids.forEach { residentId ->
            userRepository.findById(residentId)
                ?: throw IllegalArgumentException("Morador informado não foi encontrado: $residentId")
        }
    }

    private fun validateVehicles(vehicleIds: List<String>, apartmentId: String? = null) {
        val ids = vehicleIds.map { it.trim() }.filter { it.isNotBlank() }
        if (ids.size != ids.distinct().size) {
            throw IllegalArgumentException("Não é permitido repetir veiculos no apartamento.")
        }

        ids.forEach { vehicleId ->
            val vehicle = vehicleRepository.findById(vehicleId)
                ?: throw IllegalArgumentException("Veiculo informado não foi encontrado: $vehicleId")

            if (vehicle.visitorId.isNotBlank()) {
                throw IllegalArgumentException("O veiculo $vehicleId está vinculado a um visitante e não pode ser associado ao apartamento.")
            }

            if (vehicle.apartmentId.isNotBlank() && vehicle.apartmentId != apartmentId) {
                throw IllegalArgumentException("O veiculo $vehicleId já esta vinculado a outro apartamento.")
            }
        }
    }

    private fun syncVehicles(apartmentId: String, currentVehicleIds: List<String>, previousVehicleIds: List<String>) {
        val currentIds = currentVehicleIds.distinct()
        val previousIds = previousVehicleIds.distinct()

        previousIds.filter { it !in currentIds }.forEach { vehicleId ->
            val vehicle = vehicleRepository.findById(vehicleId)
            if (vehicle != null && vehicle.apartmentId == apartmentId) {
                vehicle.apartmentId = ""
                vehicleRepository.update(vehicleId, vehicle)
            }
        }

        currentIds.forEach { vehicleId ->
            val vehicle = vehicleRepository.findById(vehicleId)
                ?: throw IllegalArgumentException("Veiculo informado não foi encontrado: $vehicleId")
            if (vehicle.apartmentId != apartmentId) {
                vehicle.apartmentId = apartmentId
                vehicle.visitorId = ""
                vehicleRepository.update(vehicleId, vehicle)
            }
        }
    }

    private fun normalizeApartment(apartment: Apartment) {
        apartment.number = apartment.number.trim()
        apartment.block = apartment.block.trim().uppercase()
        apartment.floor = apartment.floor.trim()
        apartment.notes = apartment.notes.trim()
        apartment.residentIds = apartment.residentIds.map { it.trim() }.filter { it.isNotBlank() }
        apartment.vehicleIds = apartment.vehicleIds.map { it.trim() }.filter { it.isNotBlank() }
        validateVehicles(apartment.vehicleIds, apartment.id)
    }
}
