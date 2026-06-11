package portaria.project.utils

private val emailRegex = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")

fun emailValido(email: String): Boolean = emailRegex.matches(email.trim())


fun senhaValida(senha: String): Boolean =
    senha.length >= 8 && senha.any { it.isLetter() } && senha.any { it.isDigit() }


fun cpfValido(cpf: String): Boolean {
    val d = cpf.filter { it.isDigit() }
    if (d.length != 11 || d.all { it == d[0] }) return false
    var s = (0..8).sumOf { d[it].digitToInt() * (10 - it) }
    var r = 11 - (s % 11); if (r >= 10) r = 0
    if (r != d[9].digitToInt()) return false
    s = (0..9).sumOf { d[it].digitToInt() * (11 - it) }
    r = 11 - (s % 11); if (r >= 10) r = 0
    return r == d[10].digitToInt()
}


fun isoParaBr(iso: String): String {
    val p = iso.trim().split("-")
    return if (p.size == 3 && p[0].length == 4) "${p[2]}/${p[1]}/${p[0]}" else iso
}


fun brParaIso(br: String): String {
    val p = br.trim().split("/")
    return if (p.size == 3 && p[2].length == 4) "${p[2]}-${p[1]}-${p[0]}" else br
}
