package com.piggylabs.nexscene.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions

fun firebaseAuthWithGoogle(
    idToken: String,
    context: Context,
    onSuccess: (uid: String, userName: String?) -> Unit,
    onFailure: (String) -> Unit

) {
    val editor = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).edit()

    Log.d("FIRESTORE", "firebaseAuthWithGoogle() called")
    Log.d("FIRESTORE", "Received idToken=${idToken.take(20)}...")

    val credential = GoogleAuthProvider.getCredential(idToken, null)

    Log.d("FIRESTORE", "Firebase credential created")

    FirebaseAuth.getInstance()
        .signInWithCredential(credential)
        .addOnSuccessListener { result ->

            Log.d("FIRESTORE", "✅ Firebase Auth SUCCESS")

            val user = result.user
            if (user == null) {
                Log.e("FIRESTORE", "❌ Firebase user is NULL after success")
                return@addOnSuccessListener
            }

            Log.d("FIRESTORE", "Firebase UID=${user.uid}")
            Log.d("FIRESTORE", "Firebase email=${user.email}")
            Log.d("FIRESTORE", "Firebase name=${user.displayName}")
            Log.d("FIRESTORE", "Providers=${user.providerData}")

            val sharedPref = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
            val accountType = sharedPref.getString("account_type", "personal")?.lowercase() ?: "personal"
            Log.d("FIRESTORE", "Local accountType=$accountType")

            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("users").document(user.uid)
            val resolvedName = user.displayName ?: "User"
            val updates = mapOf(
                "uid" to user.uid,
                "email" to user.email,
                "userName" to resolvedName,
                "accountType" to accountType,
                "accountTypes" to FieldValue.arrayUnion(accountType),
                "userNames.$accountType" to resolvedName,
                "updatedAt" to System.currentTimeMillis(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            ref.set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    editor.putBoolean("is_logged_in", true)
                    editor.putString("uid", user.uid)
                    editor.putString("userName", resolvedName)
                    editor.apply()

                    onSuccess(user.uid, resolvedName)
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "❌ Firestore profile upsert failed", e)
                    val message = if (
                        e is FirebaseFirestoreException &&
                        e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    ) {
                        "Permission denied while creating profile. Check Firestore rules for users/{uid}."
                    } else {
                        e.message ?: "Failed to save profile"
                    }
                    onFailure(message)
                }
        }
        .addOnFailureListener { e ->
            Log.e("FIRESTORE", "❌ Firebase Auth FAILED", e)
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            onFailure(e.message ?: "Google sign-in failed")
        }
}
