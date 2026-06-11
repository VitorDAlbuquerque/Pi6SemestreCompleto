package com.example.demo.repository

import com.example.demo.model.Prestador
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class PrestadorRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(prestador: Prestador): String {
        val docRef = db.collection("prestadores").document()
        prestador.id = docRef.id
        docRef.set(prestador)
        return prestador.id!!
    }

    fun findAll(): List<Prestador> {
        val result = db.collection("prestadores").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(Prestador::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): Prestador? {
        val doc = db.collection("prestadores").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(Prestador::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun update(id: String, prestador: Prestador) {
        prestador.id = id
        db.collection("prestadores").document(id).set(prestador)
    }

    fun delete(id: String) {
        db.collection("prestadores").document(id).delete()
    }
}