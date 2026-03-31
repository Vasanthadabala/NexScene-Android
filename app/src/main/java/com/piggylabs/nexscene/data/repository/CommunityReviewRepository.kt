package com.piggylabs.nexscene.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.piggylabs.nexscene.data.model.CommunityRatingSummary
import com.piggylabs.nexscene.data.model.CommunityReview
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CommunityReviewRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeReviews(
        itemId: Int,
        mediaType: String,
        onResult: (List<CommunityReview>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return titleDocument(itemId = itemId, mediaType = mediaType)
            .collection("reviews")
            .orderBy("updatedAtMillis", Query.Direction.DESCENDING)
            .limit(60)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Unable to load community reviews")
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents.orEmpty().map { doc ->
                    CommunityReview(
                        userId = doc.getString("userId").orEmpty(),
                        userName = doc.getString("userName").orEmpty().ifBlank { "Anonymous" },
                        userPhotoUrl = doc.getString("userPhotoUrl"),
                        rating = (doc.getLong("rating") ?: 0L).toInt().coerceIn(0, 10),
                        comment = doc.getString("comment").orEmpty(),
                        updatedAtMillis = doc.getLong("updatedAtMillis") ?: 0L
                    )
                }.filter { it.comment.isNotBlank() }
                onResult(reviews)
            }
    }

    fun observeSummary(
        itemId: Int,
        mediaType: String,
        onResult: (CommunityRatingSummary) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return titleDocument(itemId = itemId, mediaType = mediaType)
            .collection("meta")
            .document("stats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.message ?: "Unable to load community rating")
                    return@addSnapshotListener
                }

                val summary = CommunityRatingSummary(
                    averageRating = snapshot?.getDouble("avgRating") ?: 0.0,
                    ratingCount = (snapshot?.getLong("ratingCount") ?: 0L).toInt().coerceAtLeast(0)
                )
                onResult(summary)
            }
    }

    suspend fun submitOrUpdateReview(
        itemId: Int,
        mediaType: String,
        userId: String,
        userName: String,
        userPhotoUrl: String?,
        rating: Int,
        comment: String
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val clampedRating = rating.coerceIn(1, 10)
        val safeComment = comment.trim()
        val now = System.currentTimeMillis()
        val titleRef = titleDocument(itemId = itemId, mediaType = mediaType)
        val reviewRef = titleRef.collection("reviews").document(userId)
        val statsRef = titleRef.collection("meta").document("stats")

        firestore.runTransaction { transaction ->
            val oldReview = transaction.get(reviewRef)
            val previousRating = (oldReview.getLong("rating") ?: 0L).toInt().coerceIn(0, 10)
            val hasPrevious = oldReview.exists()
            val existingComment = oldReview.getString("comment").orEmpty()
            val finalComment = if (safeComment.isNotBlank()) safeComment else existingComment

            val statsSnapshot = transaction.get(statsRef)
            val oldSum = statsSnapshot.getDouble("ratingSum") ?: 0.0
            val oldCount = (statsSnapshot.getLong("ratingCount") ?: 0L).toInt().coerceAtLeast(0)

            val newSum = if (hasPrevious) {
                oldSum - previousRating + clampedRating
            } else {
                oldSum + clampedRating
            }
            val newCount = if (hasPrevious) oldCount else oldCount + 1
            val avg = if (newCount > 0) newSum / newCount else 0.0

            val reviewPayload = hashMapOf<String, Any>(
                "userId" to userId,
                "userName" to userName.ifBlank { "Anonymous" },
                "rating" to clampedRating,
                "comment" to finalComment,
                "updatedAtMillis" to now
            )
            if (!userPhotoUrl.isNullOrBlank()) {
                reviewPayload["userPhotoUrl"] = userPhotoUrl
            }
            if (!hasPrevious) {
                reviewPayload["createdAtMillis"] = now
            }

            val statsPayload = hashMapOf<String, Any>(
                "itemId" to itemId,
                "mediaType" to mediaType.lowercase().ifBlank { "movie" },
                "ratingSum" to newSum,
                "ratingCount" to newCount,
                "avgRating" to avg,
                "updatedAtMillis" to now
            )

            transaction.set(reviewRef, reviewPayload)
            transaction.set(statsRef, statsPayload)
            null
        }.addOnSuccessListener {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
        }.addOnFailureListener { error ->
            if (continuation.isActive) {
                continuation.resume(Result.failure(error))
            }
        }
    }

    suspend fun deleteReview(
        itemId: Int,
        mediaType: String,
        userId: String
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val titleRef = titleDocument(itemId = itemId, mediaType = mediaType)
        val reviewRef = titleRef.collection("reviews").document(userId)
        val now = System.currentTimeMillis()

        firestore.runTransaction { transaction ->
            val reviewSnapshot = transaction.get(reviewRef)
            if (!reviewSnapshot.exists()) return@runTransaction null

            val rating = (reviewSnapshot.getLong("rating") ?: 0L).toInt()
            if (rating !in 1..10) return@runTransaction null
            val userName = reviewSnapshot.getString("userName").orEmpty().ifBlank { "Anonymous" }
            val userPhotoUrl = reviewSnapshot.getString("userPhotoUrl")

            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "userName" to userName,
                "rating" to rating,
                "comment" to "",
                "updatedAtMillis" to now
            )
            if (!userPhotoUrl.isNullOrBlank()) {
                payload["userPhotoUrl"] = userPhotoUrl
            }

            transaction.set(reviewRef, payload)
            null
        }.addOnSuccessListener {
            if (continuation.isActive) continuation.resume(Result.success(Unit))
        }.addOnFailureListener { error ->
            if (continuation.isActive) continuation.resume(Result.failure(error))
        }
    }

    private fun titleDocument(itemId: Int, mediaType: String) =
        firestore.collection("titles")
            .document("${mediaType.lowercase().ifBlank { "movie" }}_$itemId")
}
