package com.example.demo.service

import com.example.demo.model.Ocorrencia
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.OcorrenciaRepository
import org.springframework.stereotype.Service

@Service
class OcorrenciaService(
    private val repository: OcorrenciaRepository,
    private val apartmentRepository: ApartmentRepository
) {

    fun create(ocorrencia: Ocorrencia): String {
        normalize(ocorrencia)
        validate(ocorrencia)
        return repository.save(ocorrencia)
    }

    fun getAll(apartmentId: String? = null): List<Ocorrencia> {
        return if (!apartmentId.isNullOrBlank()) {
            repository.findAllByApartmentId(apartmentId)
        } else {
            repository.findAll()
        }
    }

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Ocorrencia nao encontrada.")

    fun update(id: String, ocorrencia: Ocorrencia) {
        repository.findById(id) ?: throw NoSuchElementException("Ocorrencia nao encontrada.")
        normalize(ocorrencia)
        validate(ocorrencia)
        repository.update(id, ocorrencia)
    }

    fun delete(id: String) {
        repository.findById(id) ?: throw NoSuchElementException("Ocorrencia nao encontrada.")
        repository.delete(id)
    }

    private fun validate(o: Ocorrencia) {
        if (o.title.isBlank()) {
            throw IllegalArgumentException("Titulo da ocorrencia e obrigatorio.")
        }
        if (o.description.isBlank()) {
            throw IllegalArgumentException("Descricao da ocorrencia e obrigatoria.")
        }
        if (o.category.isBlank()) {
            throw IllegalArgumentException("Categoria da ocorrencia e obrigatoria.")
        }
        if (o.reportedBy.isBlank()) {
            throw IllegalArgumentException("Responsavel pelo registro e obrigatorio.")
        }
        if (o.status.isBlank()) {
            throw IllegalArgumentException("Status da ocorrencia e obrigatorio.")
        }
        if (o.apartmentId.isNotBlank()) {
            apartmentRepository.findById(o.apartmentId)
                ?: throw IllegalArgumentException("Apartamento informado nao foi encontrado.")
        }
    }

    private fun normalize(o: Ocorrencia) {
        o.title = o.title.trim()
        o.description = o.description.trim()
        o.category = o.category.trim().uppercase()
        o.severity = o.severity.trim().uppercase().ifBlank { "MEDIA" }
        o.status = o.status.trim().uppercase().ifBlank { "ABERTA" }
        o.reportedBy = o.reportedBy.trim()
        o.apartmentId = o.apartmentId.trim()
        o.createdAt = o.createdAt.trim()
        o.resolvedAt = o.resolvedAt.trim()
        o.notes = o.notes.trim()
    }
}
