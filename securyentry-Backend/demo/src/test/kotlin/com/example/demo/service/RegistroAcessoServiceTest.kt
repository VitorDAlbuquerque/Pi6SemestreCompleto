package com.example.demo.service

import com.example.demo.model.RegistroAcesso
import com.example.demo.repository.RegistroAcessoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RegistroAcessoServiceTest {

    // mock do repositorio pra nao precisar subir o firebase nos testes
    private val repo: RegistroAcessoRepository = mock()
    private val service = RegistroAcessoService(repo)

    @Test
    fun `registrar acesso com nome valido deve salvar e retornar id`() {
        val reg = RegistroAcesso(
            pessoaNome = "Maria Silva",
            pessoaTipo = "MORADOR",
            veiculoPlaca = "ABC1D23",
            tipoEvento = "ENTRADA",
            status = "AUTORIZADO"
        )
        whenever(repo.save(reg)).thenReturn("registro-1")

        val resultado = service.create(reg)

        assertEquals("registro-1", resultado)
        verify(repo).save(reg)
    }

    @Test
    fun `nome em branco deve lancar excecao`() {
        val reg = RegistroAcesso(pessoaNome = "   ")
        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(reg) }
        assertEquals("O nome da pessoa é obrigatório.", ex.message)
    }

    @Test
    fun `buscar por id existente retorna o registro`() {
        val reg = RegistroAcesso(id = "registro-1", pessoaNome = "Joao")
        whenever(repo.findById("registro-1")).thenReturn(reg)

        val resultado = service.getById("registro-1")

        assertEquals(reg, resultado)
    }

    @Test
    fun `buscar id que nao existe deve lancar NoSuchElement`() {
        whenever(repo.findById("abc")).thenReturn(null)
        val ex = assertThrows(NoSuchElementException::class.java) { service.getById("abc") }
        assertEquals("Registro não encontrado.", ex.message)
    }
}
