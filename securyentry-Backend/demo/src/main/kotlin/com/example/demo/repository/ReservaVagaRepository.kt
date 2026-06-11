package com.example.demo.repository

import com.example.demo.model.ReservaVaga
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository

@Repository
class ReservaVagaRepository {

    private val db: Firestore by lazy {
        FirestoreClient.getFirestore()
    }

    fun save(reserva: ReservaVaga): String {
        val docRef = db.collection("reservasVagas").document()
        reserva.id = docRef.id
        docRef.set(reserva)
        return reserva.id!!
    }

    fun findAll(): List<ReservaVaga> {
        val result = db.collection("reservasVagas").get().get()
        return result.mapNotNull { doc ->
            doc.toObject(ReservaVaga::class.java)?.apply { id = doc.id }
        }
    }

    fun findAllByApartmentId(apartmentId: String): List<ReservaVaga> {
        val result = db.collection("reservasVagas")
            .whereEqualTo("apartmentId", apartmentId)
            .get()
            .get()

        return result.documents.mapNotNull { doc ->
            doc.toObject(ReservaVaga::class.java)?.apply { id = doc.id }
        }
    }

    fun findById(id: String): ReservaVaga? {
        val doc = db.collection("reservasVagas").document(id).get().get()
        return if (doc.exists()) {
            doc.toObject(ReservaVaga::class.java)?.apply { this.id = doc.id }
        } else {
            null
        }
    }

    fun update(id: String, reserva: ReservaVaga) {
        reserva.id = id
        db.collection("reservasVagas").document(id).set(reserva)
    }

    fun delete(id: String) {
        db.collection("reservasVagas").document(id).delete()
    }
}
