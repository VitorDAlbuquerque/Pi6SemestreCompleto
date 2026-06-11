package com.example.demo.service


import com.example.demo.model.RegistroAcesso
import com.example.demo.repository.RegistroAcessoRepository
import org.springframework.stereotype.Service

// Serviço e lógica de negócio para auditoria de acesso (RegistroAcessoService)
@Service
class RegistroAcessoService(
    private val repository: RegistroAcessoRepository
) {
    fun create(registro: RegistroAcesso): String {
        // Validação obrigatória do nome do indivíduo solicitando acesso
        if (registro.pessoaNome.isBlank()) {
            throw IllegalArgumentException("O nome da pessoa é obrigatório.")
        }
        return repository.save(registro)
    }

    fun getAll(): List<RegistroAcesso> = repository.findAll()

    fun getById(id: String): RegistroAcesso =
        repository.findById(id) ?: throw NoSuchElementException("Registro não encontrado.")
}