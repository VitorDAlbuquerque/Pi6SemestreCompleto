package com.example.demo.controller


import com.example.demo.model.RegistroAcesso
import com.example.demo.service.RegistroAcessoService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/historico")
class RegistroAcessoController(private val service: RegistroAcessoService) {

    @PostMapping
    fun create(@RequestBody registro: RegistroAcesso) = service.create(registro)

    @GetMapping
    fun getAll() = service.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)
}