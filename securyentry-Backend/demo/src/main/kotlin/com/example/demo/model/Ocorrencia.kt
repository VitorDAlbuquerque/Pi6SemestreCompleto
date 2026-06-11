package com.example.demo.model

data class Ocorrencia(
    var id: String? = null,
    var title: String = "",
    var description: String = "",
    var category: String = "",
    var severity: String = "MEDIA",
    var status: String = "ABERTA",
    var reportedBy: String = "",
    var apartmentId: String = "",
    var createdAt: String = "",
    var resolvedAt: String = "",
    var notes: String = ""
)
