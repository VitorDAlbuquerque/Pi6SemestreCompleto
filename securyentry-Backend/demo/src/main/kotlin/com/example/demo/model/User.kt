package com.example.demo.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.cloud.firestore.annotation.IgnoreExtraProperties


@IgnoreExtraProperties
@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    var id: String? = null,
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var phone: String = "",
    var birthDate: String = "",
    var cpf: String = "",
    @JsonProperty("role") var role: String = "MORADOR", // Mapeamento explicito Jackson para desserializacao segura
    var residentType: String = "PROPRIETARIO",
    var isActive: Boolean = true,
    var apartmentId: String? = null,
    var block: String? = null
)