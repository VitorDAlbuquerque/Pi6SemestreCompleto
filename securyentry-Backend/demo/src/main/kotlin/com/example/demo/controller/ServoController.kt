package com.example.demo.controller

import com.example.demo.service.ServoService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/servo")
class ServoController(private val service: ServoService) {

    @PostMapping("/abrir")
    fun abrir() = service.abrirPortao()
}
