package com.example.demo.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.cloud.firestore.annotation.IgnoreExtraProperties


@IgnoreExtraProperties
@JsonIgnoreProperties(ignoreUnknown = true)
data class Apartment(
    var id: String? = null,
    var number: String = "",
    var block: String = "",
    var floor: String = "",
    var residentIds: List<String> = emptyList(),
    var maxResidents: Int = 0,
    var isOccupied: Boolean = false,
    var parkingSpotCount: Int = 0,
    var availableParkingSpots: Int = 0,
    var notes: String = "",
    var isActive: Boolean = true
) {
    var vehicleIds: List<String> = emptyList()
}