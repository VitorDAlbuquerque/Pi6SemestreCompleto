package com.example.demo.service

import com.example.demo.model.Prestador
import com.example.demo.repository.PrestadorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PrestadorServiceTest {

    private val repo: PrestadorRepository = mock()
    private val service = PrestadorService(repo)

    @Test
    fun `prestador com empresa e funcionario deve ser cadastrado`() {
        val p = Prestador(
            companyName = "Manutencao Predial LTDA",
            employeeName = "Ana Costa",
            serviceType = "MANUTENCAO",
            allowedStartTime = "08:00",
            allowedEndTime = "18:00"
        )
        whenever(repo.save(p)).thenReturn("prestador-1")

        val id = service.create(p)

        assertEquals("prestador-1", id)
        verify(repo).save(p)
    }

    @Test
    fun `prestador sem nome de empresa deve ser rejeitado`() {
        val p = Prestador(companyName = "", employeeName = "Ana Costa")
        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(p) }
        assertEquals("O nome da empresa é obrigatório.", ex.message)
    }

    @Test
    fun `prestador sem nome do funcionario deve ser rejeitado`() {
        val p = Prestador(companyName = "Manutencao Predial LTDA", employeeName = "")
        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(p) }
        assertEquals("O nome do funcionário é obrigatório.", ex.message)
    }
}
