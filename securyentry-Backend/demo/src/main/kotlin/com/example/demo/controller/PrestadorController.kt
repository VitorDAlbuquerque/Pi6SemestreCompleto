package com.example.demo.controller


import com.example.demo.model.Prestador
import com.example.demo.service.PrestadorService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/prestadores")
class PrestadorController(private val service: PrestadorService) {

    @PostMapping
    fun create(@RequestBody prestador: Prestador) = service.create(prestador)

    @GetMapping
    fun getAll() = service.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody prestador: Prestador) {
        service.update(id, prestador)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        service.delete(id)
    }
}