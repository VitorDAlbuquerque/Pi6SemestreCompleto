package com.example.demo.controller


import com.example.demo.model.Visitor
import com.example.demo.service.VisitorService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/visitors")
class VisitorController(private val service: VisitorService) {

    @PostMapping
    fun create(@RequestBody visitor: Visitor) = service.create(visitor)

    @GetMapping
    fun getAll(@RequestParam(required = false) apartmentId: String?) = service.getAll(apartmentId)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody visitor: Visitor) {
        service.update(id, visitor)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        service.delete(id)
    }
}
