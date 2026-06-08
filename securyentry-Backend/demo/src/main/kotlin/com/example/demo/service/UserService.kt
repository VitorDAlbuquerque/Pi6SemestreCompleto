package com.example.demo.service


import com.example.demo.dto.LoginRequest
import com.example.demo.dto.LoginResponse
import com.example.demo.model.User
import com.example.demo.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

// Serviço de usuários e autenticação (UserService)
@Service
class UserService(private val repository: UserRepository) {

    private val passwordEncoder = BCryptPasswordEncoder()

    fun create(user: User): User {
        // Tracing operacional de seguranca para criacao de conta
        println("BACKEND_CREATE -> Role recebida do Frontend: '${user.role}'")

        val email = user.email.trim().lowercase()
        validateUser(user)

        if (repository.findAllByEmail(email).isNotEmpty()) {
            throw IllegalArgumentException("Já existe uma conta com esse e-mail.")
        }

        user.email = email
        user.password = hash(user.password)

        // Garantia de segurança (Sanitização)
        user.role = if (user.role.uppercase() == "ADMIN") "ADMIN" else "MORADOR"

        println("BACKEND_CREATE -> Role que será enviada ao Firebase: '${user.role}'")

        val generatedId = repository.save(user)
        user.id = generatedId
        return user
    }

    fun getAll() = repository.findAll()

    fun getById(id: String) =
        repository.findById(id) ?: throw NoSuchElementException("Usuário não encontrado.")

    fun update(id: String, user: User) {
        val existingUser = repository.findById(id) ?: throw NoSuchElementException("Usuário não encontrado.")
        val email = user.email.trim().lowercase()
        val currentEmail = existingUser.email.trim().lowercase()
        validateUser(user, isUpdate = true)

        user.email = email
        user.password = if (user.password.isBlank()) {
            existingUser.password
        } else {
            hash(user.password)
        }

        if (email == currentEmail) {
            repository.update(id, user)
            return
        }

        val usersWithSameEmail = repository.findAllByEmail(email)
        val emailBelongsToAnotherUser = usersWithSameEmail.any { it.id != id }
        if (emailBelongsToAnotherUser) {
            throw IllegalArgumentException("Já existe uma conta com esse e-mail.")
        }

        repository.update(id, user)
    }

    fun delete(id: String) = repository.delete(id)

    fun login(request: LoginRequest): LoginResponse {
        val email = request.email.trim().lowercase()
        val password = request.password.trim()

        if (email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("E-mail e senha são obrigatórios.")
        }

        val user = repository.findByEmail(email)
            ?: throw NoSuchElementException("Não encontramos uma conta com esse e-mail.")

        if (!user.isActive) {
            throw IllegalAccessException("Sua conta está desativada. Entre em contato com o administrador.")
        }

        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("Senha inválida. Verifique e tente novamente.")
        }

        println("BACKEND_LOGIN -> Lendo do Firebase, o Role do usuário é: '${user.role}'")

        return LoginResponse(
            message = "Login realizado com sucesso.",
            userId = user.id.orEmpty(),
            name = user.name,
            email = user.email,
            role = user.role,
            apartmentId = user.apartmentId,
            block = user.block
        )
    }

    private fun validateUser(user: User, isUpdate: Boolean = false) {
        if (user.name.isBlank()) throw IllegalArgumentException("O nome é obrigatório.")
        if (user.email.isBlank()) throw IllegalArgumentException("O e-mail é obrigatório.")
        if (!user.email.contains("@")) throw IllegalArgumentException("Insira um e-mail válido.")
        if (!isUpdate && user.password.isBlank()) throw IllegalArgumentException("A senha é obrigatória.")
        if (!isUpdate && user.password.length < 6) throw IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.")
        if (user.cpf.isBlank()) throw IllegalArgumentException("O CPF é obrigatório.")
    }

    private fun hash(value: String): String {
        return requireNotNull(passwordEncoder.encode(value))
    }
}