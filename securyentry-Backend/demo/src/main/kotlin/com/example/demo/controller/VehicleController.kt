package com.example.demo.controller


import com.example.demo.model.Vehicle
import com.example.demo.service.VehicleService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/vehicles")
class VehicleController(private val service: VehicleService) {

    @PostMapping
    fun create(@RequestBody vehicle: Vehicle) = service.create(vehicle)

    @GetMapping
    fun getAll(@RequestParam(required = false) apartmentId: String?) = service.getAll(apartmentId)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody vehicle: Vehicle) {
        service.update(id, vehicle)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) {
        service.delete(id)
    }
}
