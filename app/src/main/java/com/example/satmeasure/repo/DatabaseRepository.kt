package com.example.satmeasure.repo

import com.example.satmeasure.model.MeasurementRecord
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class DatabaseRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    suspend fun saveMeasurement(userId: String, record: MeasurementRecord): Result<String> {
        return try {
            val measurementsRef = usersRef.child(userId)
                .child("measurements")
            val recordRef = if (record.id.isNotEmpty()) {
                measurementsRef.child(record.id)
            } else {
                measurementsRef.push()
            }
            val id = recordRef.key ?: return Result.failure(Exception("Failed to generate key"))

            record.id = id
            record.userId = userId

            recordRef.setValue(record)
                .await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMeasurements(userId: String): Result<List<MeasurementRecord>> {
        return try {
            val snapshot = usersRef.child(userId)
                .child("measurements")
                .get()
                .await()
            val records = mutableListOf<MeasurementRecord>()
            for (child in snapshot.children) {
                val record = child.getValue(MeasurementRecord::class.java)
                if (record != null) {
                    records.add(
                        record.copy(id = child.key ?: "")
                    ) // populate id temporarily just in case, though record.id exists
                }
            }
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSharedMeasurement(ownerId: String, plotId: String): Result<MeasurementRecord> {
        return try {
            val snapshot = usersRef.child(ownerId)
                .child("measurements")
                .child(plotId)
                .get()
                .await()
            val record = snapshot.getValue(MeasurementRecord::class.java)
            if (record != null) {
                Result.success(record.copy(id = snapshot.key ?: ""))
            } else {
                Result.failure(Exception("Measurement not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMeasurement(userId: String, recordId: String): Result<Unit> {
        return try {
            usersRef.child(userId)
                .child("measurements")
                .child(recordId)
                .removeValue()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllMeasurements(userId: String): Result<Unit> {
        return try {
            usersRef.child(userId)
                .child("measurements")
                .removeValue()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
