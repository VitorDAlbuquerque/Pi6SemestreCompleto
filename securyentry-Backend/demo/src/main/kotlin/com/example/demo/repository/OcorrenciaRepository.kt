package com.example.demo.repository

import com.example.demo.model.Ocorrencia
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class OcorrenciaRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(ocorrencia: Ocorrencia): String {
        val docRef = db.collection("ocorrencias").document()
        ocorrencia.id = docRef.id
        docRef.set(ocorrencia)
        return ocorrencia.id!!
    }

    fun findAll(): List<Ocorrencia> {
        val result = db.collection("ocorrencias").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(Ocorrencia::class.java)?.apply { id = doc.id }
        }
    }

    fun findAllByApartmentId(apartmentId: String): List<Ocorrencia> {
        val result = db.collection("ocorrencias")
            .whereEqualTo("apartmentId", apartmentId)
            .get()
            .get()

        return result.documents.mapNotNull { doc ->
            doc.toObject(Ocorrencia::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): Ocorrencia? {
        val doc = db.collection("ocorrencias").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(Ocorrencia::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun update(id: String, ocorrencia: Ocorrencia) {
        ocorrencia.id = id
        db.collection("ocorrencias").document(id).set(ocorrencia)
    }

    fun delete(id: String) {
        db.collection("ocorrencias").document(id).delete()
    }
}
