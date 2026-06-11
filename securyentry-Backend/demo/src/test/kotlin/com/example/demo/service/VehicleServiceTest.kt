package com.example.demo.service

import com.example.demo.model.Apartment
import com.example.demo.model.Vehicle
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.VehicleRepository
import com.example.demo.repository.VisitorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class VehicleServiceTest {

    private val vehicleRepo: VehicleRepository = mock()
    private val aptRepo: ApartmentRepository = mock()
    private val visitorRepo: VisitorRepository = mock()
    private val service = VehicleService(vehicleRepo, aptRepo, visitorRepo)

    @Test
    fun `cadastrar veiculo com placa nova e apartamento valido`() {
        // stubs pra simular ambiente sem firebase
        val apt = Apartment(id = "apt-101", number = "101")
        val v = Vehicle(plate = " abc1d23 ", apartmentId = "apt-101", type = " carro ")

        whenever(aptRepo.findById("apt-101")).thenReturn(apt)
        whenever(vehicleRepo.findAllByPlate("ABC1D23")).thenReturn(emptyList())
        whenever(vehicleRepo.save(v)).thenReturn("vehicle-1")

        val id = service.create(v)

        assertEquals("vehicle-1", id)
        assertEquals("ABC1D23", v.plate)
        assertEquals("CARRO", v.type)
        verify(vehicleRepo).save(v)
        verify(aptRepo).update(any(), any())
    }

    @Test
    fun `veiculo sem placa nao deve ser cadastrado`() {
        val v = Vehicle(plate = "", apartmentId = "apt-101")
        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(v) }
        assertEquals("Placa do veiculo é obrigatória.", ex.message)
    }

    @Test
    fun `placa ja existente deve rejeitar cadastro`() {
        val v = Vehicle(plate = "ABC1D23")
        whenever(vehicleRepo.findAllByPlate("ABC1D23"))
            .thenReturn(listOf(Vehicle(id = "vehicle-existente", plate = "ABC1D23")))

        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(v) }

        assertEquals("Já existe um veiculo cadastrado com esta placa.", ex.message)
    }

    @Test
    fun `veiculo nao pode ter apartamento e visitante ao mesmo tempo`() {
        val v = Vehicle(plate = "ABC1D23", apartmentId = "apt-101", visitorId = "visitor-1")
        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(v) }
        assertEquals("O veiculo não pode estar vinculado ao apartamento e ao visitante ao mesmo tempo.", ex.message)
    }
}
