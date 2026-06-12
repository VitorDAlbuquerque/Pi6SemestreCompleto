package com.example.demo.repository

import com.example.demo.model.RegistroAcesso
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class RegistroAcessoRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(registro: RegistroAcesso): String {
        val docRef = db.collection("historico_acessos").document()
        registro.id = docRef.id
        docRef.set(registro)
        return registro.id!!
    }

    fun findAll(): List<RegistroAcesso> {
        val result = db.collection("historico_acessos").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(RegistroAcesso::class.java)?.apply { id = doc.id }
        }
    }

    // Histórico geralmente não é editado nem deletado por questões de segurança (Auditoria),
    // mas deixamos a busca por ID disponível.
    fun findById(id: String): RegistroAcesso? {
        val doc = db.collection("historico_acessos").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(RegistroAcesso::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun findLastByPlate(plate: String): RegistroAcesso? {
        val result = db.collection("historico_acessos")
            .whereEqualTo("veiculoPlaca", plate)
            .get()
            .get()
        return result.documents
            .mapNotNull { doc -> doc.toObject(RegistroAcesso::class.java)?.apply { this.id = doc.id } }
            .maxByOrNull { it.dataHora }
    }
}