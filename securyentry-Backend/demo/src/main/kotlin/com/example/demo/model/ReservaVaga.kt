package com.example.demo.model

data class ReservaVaga(
    var id: String? = null,
    var apartmentId: String = "",
    var residentName: String = "",
    var spotNumber: String = "",
    var vehiclePlate: String = "",
    var startDateTime: String = "",
    var endDateTime: String = "",
    var status: String = "PENDENTE",
    var notes: String = ""
)
