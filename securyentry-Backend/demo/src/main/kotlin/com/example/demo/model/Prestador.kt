package com.example.demo.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class Prestador(
    var id: String? = null,
    var companyName: String = "",
    var employeeName: String = "",
    var cpf: String = "",
    var phone: String = "",
    var serviceType: String = "",
    var allowedStartTime: String = "08:00",
    var allowedEndTime: String = "18:00",
    var status: String = "ATIVO",
    var notes: String = "",
    var isActive: Boolean = true
)