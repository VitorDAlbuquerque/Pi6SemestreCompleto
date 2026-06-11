package com.example.demo.model


data class RegistroAcesso(
    var id: String? = null,
    var tipoEvento: String = "ENTRADA",
    var pessoaNome: String = "",  // <--- É este o nome que o seu Service procura
    var pessoaTipo: String = "MORADOR",
    var veiculoPlaca: String = "",
    var dataHora: String = "",
    var status: String = "AUTORIZADO",
    var observacao: String = ""
)