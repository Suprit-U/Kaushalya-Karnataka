package com.kaushalyakarnataka.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entities ───────────────────────────────────────────────

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val category: String,
    val role: String,
    val bio: String,
    val rating: Double,
    val reviewCount: Int,
    val experienceYears: Int,
    val successRate: Int,
    val pricePerHour: Int,
    val distanceKm: Double,
    val isAvailable: Boolean,
    val isVerified: Boolean,
    val isGovernmentCertified: Boolean,
    val tags: String, // JSON array string
    val avatarUrl: String,
    val phone: String,
    val location: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val workerId: String,
    val workerName: String,
    val service: String,
    val scheduledDate: Long,
    val timeSlot: String,
    val address: String,
    val notes: String,
    val status: String,
    val estimatedCostMin: Int,
    val estimatedCostMax: Int,
    val bookingCode: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val workerId: String,
    val customerId: String,
    val customerName: String,
    val customerInitial: String,
    val customerAvatarUrl: String,
    val rating: Int,
    val comment: String,
    val serviceType: String,
    val helpfulCount: Int,
    val isVerified: Boolean,
    val createdAt: Long,
    val cachedAt: Long = System.currentTimeMillis()
)

// ─── DAOs ────────────────────────────────────────────────────

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY rating DESC")
    fun getAllWorkers(): Flow<List<WorkerEntity>>

    @Query("SELECT * FROM workers WHERE uid = :uid LIMIT 1")
    suspend fun getWorkerById(uid: String): WorkerEntity?

    @Query("SELECT * FROM workers WHERE category = :category ORDER BY rating DESC")
    fun getWorkersByCategory(category: String): Flow<List<WorkerEntity>>

    @Upsert
    suspend fun upsertWorkers(workers: List<WorkerEntity>)

    @Upsert
    suspend fun upsertWorker(worker: WorkerEntity)

    @Query("DELETE FROM workers WHERE cachedAt < :expiry")
    suspend fun deleteExpired(expiry: Long)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE customerId = :id ORDER BY scheduledDate DESC")
    fun getCustomerBookings(id: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE workerId = :id ORDER BY scheduledDate ASC")
    fun getWorkerBookings(id: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE workerId = :id AND status = 'PENDING'")
    fun getPendingBookings(id: String): Flow<List<BookingEntity>>

    @Upsert
    suspend fun upsertBookings(bookings: List<BookingEntity>)

    @Upsert
    suspend fun upsertBooking(booking: BookingEntity)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE workerId = :workerId ORDER BY createdAt DESC")
    fun getWorkerReviews(workerId: String): Flow<List<ReviewEntity>>

    @Upsert
    suspend fun upsertReviews(reviews: List<ReviewEntity>)
}

// ─── Database ─────────────────────────────────────────────────

@Database(
    entities = [WorkerEntity::class, BookingEntity::class, ReviewEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KaushalyaDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun bookingDao(): BookingDao
    abstract fun reviewDao(): ReviewDao
}
