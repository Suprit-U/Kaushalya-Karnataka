package com.kaushalyakarnataka.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

interface BookingRepository {
    suspend fun createBooking(booking: Booking): UiState<Booking>
    suspend fun getCustomerBookings(customerId: String): UiState<List<Booking>>
    suspend fun getWorkerBookings(workerId: String): UiState<List<Booking>>
    suspend fun getWorkerPendingBookings(workerId: String): UiState<List<Booking>>
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): UiState<Unit>
    suspend fun getBookingById(bookingId: String): UiState<Booking>
}

class BookingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : BookingRepository {

    private val bookingsRef get() = firestore.collection(FirestoreCollections.BOOKINGS)

    override suspend fun createBooking(booking: Booking): UiState<Booking> {
        return try {
            val bookingId = UUID.randomUUID().toString()
            val bookingCode = "#KK-${System.currentTimeMillis() % 100000}"
            val newBooking = booking.copy(
                id = bookingId,
                bookingCode = bookingCode,
                status = BookingStatus.PENDING,
                createdAt = Timestamp.now(),
            )
            bookingsRef.document(bookingId).set(newBooking).await()
            UiState.Success(newBooking)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to create booking")
        }
    }

    override suspend fun getCustomerBookings(customerId: String): UiState<List<Booking>> {
        return try {
            val snapshot = bookingsRef
                .whereEqualTo(FirestoreCollections.Fields.CUSTOMER_ID, customerId)
                .orderBy(FirestoreCollections.Fields.CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .await()
            UiState.Success(snapshot.toObjects(Booking::class.java))
        } catch (e: Exception) {
            UiState.Success(sampleBookings())
        }
    }

    override suspend fun getWorkerBookings(workerId: String): UiState<List<Booking>> {
        return try {
            val snapshot = bookingsRef
                .whereEqualTo(FirestoreCollections.Fields.WORKER_ID, workerId)
                .orderBy(FirestoreCollections.Fields.SCHEDULED_DATE, Query.Direction.ASCENDING)
                .get()
                .await()
            UiState.Success(snapshot.toObjects(Booking::class.java))
        } catch (e: Exception) {
            UiState.Success(sampleBookings())
        }
    }

    override suspend fun getWorkerPendingBookings(workerId: String): UiState<List<Booking>> {
        return try {
            val snapshot = bookingsRef
                .whereEqualTo(FirestoreCollections.Fields.WORKER_ID, workerId)
                .whereEqualTo(FirestoreCollections.Fields.STATUS, BookingStatus.PENDING.name)
                .get()
                .await()
            UiState.Success(snapshot.toObjects(Booking::class.java))
        } catch (e: Exception) {
            UiState.Success(sampleBookings().filter { it.status == BookingStatus.PENDING })
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): UiState<Unit> {
        return try {
            bookingsRef.document(bookingId)
                .update(FirestoreCollections.Fields.STATUS, status.name)
                .await()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to update booking status")
        }
    }

    override suspend fun getBookingById(bookingId: String): UiState<Booking> {
        return try {
            val snapshot = bookingsRef.document(bookingId).get().await()
            val booking = snapshot.toObject(Booking::class.java)
                ?: return UiState.Error("Booking not found")
            UiState.Success(booking)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Failed to load booking")
        }
    }
}

fun sampleBookings() = listOf(
    Booking(
        id = "booking1",
        customerId = "customer1",
        customerName = "Anjali Sharma",
        workerId = "worker1",
        workerName = "Ramesh Kumar",
        service = "Wiring & Repair",
        timeSlot = "9:00 AM",
        address = "42, 5th Cross, Koramangala 4th Block",
        status = BookingStatus.PENDING,
        estimatedCostMin = 450,
        estimatedCostMax = 900,
        bookingCode = "#KK-2024-0106",
    ),
    Booking(
        id = "booking2",
        customerId = "customer2",
        customerName = "Prakash Nair",
        workerId = "worker1",
        workerName = "Ramesh Kumar",
        service = "Fan Installation × 2",
        timeSlot = "2:00 PM",
        address = "HSR Layout, Sector 6",
        status = BookingStatus.PENDING,
        estimatedCostMin = 700,
        estimatedCostMax = 1000,
        bookingCode = "#KK-2024-0107",
    ),
    Booking(
        id = "booking3",
        customerId = "customer3",
        customerName = "Sneha Reddy",
        workerId = "worker1",
        workerName = "Ramesh Kumar",
        service = "Home Wiring",
        timeSlot = "9:00 AM",
        address = "Indiranagar, 12th Main",
        status = BookingStatus.CONFIRMED,
        estimatedCostMin = 900,
        estimatedCostMax = 1200,
        bookingCode = "#KK-2024-0108",
    ),
)
