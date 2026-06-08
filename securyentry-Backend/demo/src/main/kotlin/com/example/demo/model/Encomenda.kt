package com.example.demo.model


data class Encomenda(
    var id: String? = null,
    var description: String = "",
    var recipientName: String = "",
    var storageLocation: String = "",
    var apartmentId: String = "",
    var deliveredToResident: Boolean = false,
    var notes: String = ""
)
