package com.example.demo.service

import com.example.demo.model.ReservaVaga
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.ReservaVagaRepository
import org.springframework.stereotype.Service

@Service
class ReservaVagaService(
    private val repository: ReservaVagaRepository,
    private val apartmentRepository: ApartmentRepository
) {

    fun create(reserva: ReservaVaga): String {
        normalize(reserva)
        validate(reserva)
        return repository.save(reserva)
    }

    fun getAll(apartmentId: String? = null): List<ReservaVaga> {
        return if (!apartmentId.isNullOrBlank()) {
            repository.findAllByApartmentId(apartmentId)
        } else {
            repository.findAll()
        }
    }

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Reserva de vaga nao encontrada.")

    fun update(id: String, reserva: ReservaVaga) {
        repository.findById(id) ?: throw NoSuchElementException("Reserva de vaga nao encontrada.")
        normalize(reserva)
        validate(reserva)
        repository.update(id, reserva)
    }

    fun delete(id: String) {
        repository.findById(id) ?: throw NoSuchElementException("Reserva de vaga nao encontrada.")
        repository.delete(id)
    }

    private fun validate(r: ReservaVaga) {
        if (r.residentName.isBlank()) {
            throw IllegalArgumentException("Nome do morador e obrigatorio.")
        }
        if (r.spotNumber.isBlank()) {
            throw IllegalArgumentException("Numero da vaga e obrigatorio.")
        }
        if (r.startDateTime.isBlank()) {
            throw IllegalArgumentException("Inicio da reserva e obrigatorio.")
        }
        if (r.endDateTime.isBlank()) {
            throw IllegalArgumentException("Fim da reserva e obrigatorio.")
        }
        if (r.status.isBlank()) {
            throw IllegalArgumentException("Status da reserva e obrigatorio.")
        }
        if (r.apartmentId.isNotBlank()) {
            apartmentRepository.findById(r.apartmentId)
                ?: throw IllegalArgumentException("Apartamento informado nao foi encontrado.")
        }
    }

    private fun normalize(r: ReservaVaga) {
        r.apartmentId = r.apartmentId.trim()
        r.residentName = r.residentName.trim()
        r.spotNumber = r.spotNumber.trim()
        r.vehiclePlate = r.vehiclePlate.trim().uppercase()
        r.startDateTime = r.startDateTime.trim()
        r.endDateTime = r.endDateTime.trim()
        r.status = r.status.trim().uppercase().ifBlank { "PENDENTE" }
        r.notes = r.notes.trim()
    }
}
