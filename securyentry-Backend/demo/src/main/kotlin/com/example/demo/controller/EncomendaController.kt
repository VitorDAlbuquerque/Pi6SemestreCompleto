package com.example.demo.controller


import com.example.demo.model.Encomenda
import com.example.demo.service.EncomendaService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/encomendas")
class EncomendaController(private val service: EncomendaService) {

    @PostMapping
    fun create(@RequestBody encomenda: Encomenda) = service.create(encomenda)

    @GetMapping
    fun getAll(@RequestParam(required = false) apartmentId: String?) = service.getAll(apartmentId)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody encomenda: Encomenda) {
        service.update(id, encomenda)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        service.delete(id)
    }
}
