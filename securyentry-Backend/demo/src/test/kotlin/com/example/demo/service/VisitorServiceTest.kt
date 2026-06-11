package com.example.demo.service

import com.example.demo.model.Apartment
import com.example.demo.model.Vehicle
import com.example.demo.model.Visitor
import com.example.demo.repository.ApartmentRepository
import com.example.demo.repository.VehicleRepository
import com.example.demo.repository.VisitorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class VisitorServiceTest {

    private val visitorRepo: VisitorRepository = mock()
    private val aptRepo: ApartmentRepository = mock()
    private val vehicleRepo: VehicleRepository = mock()
    private val service = VisitorService(visitorRepo, aptRepo, vehicleRepo)

    @Test
    fun `visitante com data e apartamento valido deve ser salvo`() {
        val visitor = Visitor(
            name = "Carlos Souza",
            cpf = "12345678900",
            apartmentId = "apt-101",
            visitDate = "2026-06-10",
            authorizedBy = "Maria Silva"
        )
        whenever(aptRepo.findById("apt-101")).thenReturn(Apartment(id = "apt-101", number = "101"))
        whenever(visitorRepo.save(visitor)).thenReturn("visitor-1")

        val id = service.create(visitor)

        assertEquals("visitor-1", id)
        verify(visitorRepo).save(visitor)
    }

    @Test
    fun `visitante sem acesso livre precisa informar data`() {
        val visitor = Visitor(
            name = "Carlos Souza", cpf = "12345678900",
            apartmentId = "apt-101", freeAccess = false, visitDate = ""
        )
        whenever(aptRepo.findById("apt-101")).thenReturn(Apartment(id = "apt-101"))

        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(visitor) }

        assertEquals("A data da visita e obrigatoria para visitantes sem acesso livre.", ex.message)
    }

    @Test
    fun `visitante com veiculo que nao existe deve falhar`() {
        val visitor = Visitor(
            name = "Carlos Souza", cpf = "12345678900",
            apartmentId = "apt-101", visitDate = "2026-06-10",
            hasVehicle = true, vehicleId = "vehicle-404"
        )
        whenever(aptRepo.findById("apt-101")).thenReturn(Apartment(id = "apt-101"))
        whenever(vehicleRepo.findById("vehicle-404")).thenReturn(null)

        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(visitor) }

        assertEquals("Veiculo informado para o visitante nao foi encontrado.", ex.message)
    }

    @Test
    fun `veiculo ja vinculado a apartamento nao pode ser usado em visita`() {
        val visitor = Visitor(
            name = "Carlos Souza", cpf = "12345678900",
            apartmentId = "apt-101", visitDate = "2026-06-10",
            hasVehicle = true, vehicleId = "vehicle-1"
        )
        whenever(aptRepo.findById("apt-101")).thenReturn(Apartment(id = "apt-101"))
        whenever(vehicleRepo.findById("vehicle-1"))
            .thenReturn(Vehicle(id = "vehicle-1", apartmentId = "apt-202"))

        val ex = assertThrows(IllegalArgumentException::class.java) { service.create(visitor) }

        assertEquals("O veiculo informado ja esta vinculado a um apartamento.", ex.message)
    }
}
