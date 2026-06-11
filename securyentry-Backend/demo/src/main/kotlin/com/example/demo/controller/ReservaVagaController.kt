package com.example.demo.controller

import com.example.demo.model.ReservaVaga
import com.example.demo.service.ReservaVagaService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/reservas-vagas")
class ReservaVagaController(private val service: ReservaVagaService) {

    @PostMapping
    fun create(@RequestBody reserva: ReservaVaga) = service.create(reserva)

    @GetMapping
    fun getAll(@RequestParam(required = false) apartmentId: String?) = service.getAll(apartmentId)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody reserva: ReservaVaga) {
        service.update(id, reserva)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        service.delete(id)
    }
}
