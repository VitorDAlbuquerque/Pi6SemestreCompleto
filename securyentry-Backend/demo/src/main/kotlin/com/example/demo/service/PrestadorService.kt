package com.example.demo.service


import com.example.demo.model.Prestador
import com.example.demo.repository.PrestadorRepository
import org.springframework.stereotype.Service

// Serviço e lógica de negócio para prestadores de serviço (PrestadorService)
@Service
class PrestadorService(
    private val repository: PrestadorRepository
) {

    fun create(prestador: Prestador): String {
        validatePrestador(prestador)
        return repository.save(prestador)
    }

    fun getAll(): List<Prestador> = repository.findAll()

    fun getById(id: String): Prestador =
        repository.findById(id) ?: throw NoSuchElementException("Prestador não encontrado.")

    fun update(id: String, prestador: Prestador) {
        repository.findById(id) ?: throw NoSuchElementException("Prestador não encontrado.")
        validatePrestador(prestador)
        repository.update(id, prestador)
    }

    fun delete(id: String) {
        repository.findById(id) ?: throw NoSuchElementException("Prestador não encontrado.")
        repository.delete(id)
    }

    private fun validatePrestador(prestador: Prestador) {
        if (prestador.companyName.isBlank()) {
            throw IllegalArgumentException("O nome da empresa é obrigatório.")
        }
        if (prestador.employeeName.isBlank()) {
            throw IllegalArgumentException("O nome do funcionário é obrigatório.")
        }
    }
}