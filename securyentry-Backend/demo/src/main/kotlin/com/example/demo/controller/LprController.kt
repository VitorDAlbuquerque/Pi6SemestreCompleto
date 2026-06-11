package com.example.demo.controller

import com.example.demo.service.LprService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/lpr")
class LprController(private val service: LprService) {

    @PostMapping("/trigger")
    fun trigger() = service.triggerScan()
}
