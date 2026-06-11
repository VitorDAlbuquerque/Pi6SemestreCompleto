package com.example.demo.controller

import com.example.demo.model.Ocorrencia
import com.example.demo.service.OcorrenciaService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ocorrencias")
class OcorrenciaController(private val service: OcorrenciaService) {

    @PostMapping
    fun create(@RequestBody ocorrencia: Ocorrencia) = service.create(ocorrencia)

    @GetMapping
    fun getAll(@RequestParam(required = false) apartmentId: String?) = service.getAll(apartmentId)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody ocorrencia: Ocorrencia) {
        service.update(id, ocorrencia)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        service.delete(id)
    }
}
