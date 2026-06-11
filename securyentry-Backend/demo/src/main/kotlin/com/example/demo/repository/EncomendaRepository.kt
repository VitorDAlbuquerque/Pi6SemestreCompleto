package com.example.demo.repository

import com.example.demo.model.Encomenda
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class EncomendaRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(encomenda: Encomenda): String {
        val docRef = db.collection("encomendas").document()
        encomenda.id = docRef.id
        docRef.set(encomenda)
        return encomenda.id!!
    }

    fun findAll(): List<Encomenda> {
        val result = db.collection("encomendas").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(Encomenda::class.java)?.apply { id = doc.id }
        }
    }

    fun findAllByApartmentId(apartmentId: String): List<Encomenda> {
        val result = db.collection("encomendas")
            .whereEqualTo("apartmentId", apartmentId)
            .get()
            .get()

        return result.documents.mapNotNull { doc ->
            doc.toObject(Encomenda::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): Encomenda? {
        val doc = db.collection("encomendas").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(Encomenda::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun update(id: String, encomenda: Encomenda) {
        encomenda.id = id
        db.collection("encomendas").document(id).set(encomenda)
    }

    fun delete(id: String) {
        db.collection("encomendas").document(id).delete()
    }
}