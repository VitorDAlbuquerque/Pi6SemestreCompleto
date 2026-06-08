package com.example.demo.service


import com.example.demo.model.Encomenda
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.EncomendaRepository
import org.springframework.stereotype.Service

// Serviço e lógica de negócio para encomendas (EncomendaService)
@Service
class EncomendaService(
    private val repository: EncomendaRepository,
    private val apartmentRepository: ApartmentRepository
) {

    fun create(encomenda: Encomenda): String {
        normalize(encomenda)
        validate(encomenda)
        return repository.save(encomenda)
    }

    fun getAll(apartmentId: String? = null): List<Encomenda> {
        return if (!apartmentId.isNullOrBlank()) {
            repository.findAllByApartmentId(apartmentId)
        } else {
            repository.findAll()
        }
    }

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Encomenda não encontrada.")

    fun update(id: String, encomenda: Encomenda) {
        repository.findById(id) ?: throw NoSuchElementException("Encomenda não encontrada.")
        normalize(encomenda)
        validate(encomenda)
        repository.update(id, encomenda)
    }

    fun delete(id: String) {
        repository.findById(id) ?: throw NoSuchElementException("Encomenda não encontrada.")
        repository.delete(id)
    }

    private fun validate(e: Encomenda) {
        if (e.description.isBlank()) {
            throw IllegalArgumentException("Descrição da encomenda é obrigatória.")
        }
        if (e.recipientName.isBlank()) {
            throw IllegalArgumentException("Destinatario é obrigatório.")
        }
        if (e.apartmentId.isNotBlank()) {
            apartmentRepository.findById(e.apartmentId)
                ?: throw IllegalArgumentException("Apartamento informado não foi encontrado.")
        }
    }

    private fun normalize(e: Encomenda) {
        e.description = e.description.trim()
        e.recipientName = e.recipientName.trim()
        e.storageLocation = e.storageLocation.trim()
        e.apartmentId = e.apartmentId.trim()
        e.notes = e.notes.trim()
    }
}