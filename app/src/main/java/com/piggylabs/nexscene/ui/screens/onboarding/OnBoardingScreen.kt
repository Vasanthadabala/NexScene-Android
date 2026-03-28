package com.piggylabs.nexscene.ui.screens.onboarding

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.piggylabs.nexscene.R
import com.piggylabs.nexscene.auth.firebaseAuthWithGoogle
import com.piggylabs.nexscene.data.local.db.AppDataBase
import com.piggylabs.nexscene.data.local.entity.TitleStateEntity
import com.piggylabs.nexscene.navigation.Home
import com.piggylabs.nexscene.ui.theme.appColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

private const val LOGIN_OPTIONS_TAG = "LoginOptionsFlow"
private const val LAST_SYNCED_AT_KEY = "last_synced_at"
private const val ACTIVE_DATA_UID_KEY = "active_data_uid"
private const val ACTIVE_DATA_ACCOUNT_TYPE_KEY = "active_data_account_type"

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreen(navController: NavHostController) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            OnBoardingScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreenComponent(navController: NavHostController) {

    val context = LocalContext.current

    val prefs = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val editor = prefs.edit()

    val webClientId = context.getString(R.string.default_web_client_id)
    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(webClientId)
                .build()
        )
    }

    var isGoogleSigningIn by remember { mutableStateOf(false) }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(LOGIN_OPTIONS_TAG, "Google launcher completed: resultCode=${result.resultCode}, mode=login")
        isGoogleSigningIn = false

        if (result.resultCode != Activity.RESULT_OK) {
            Log.e(LOGIN_OPTIONS_TAG, "Google Sign-In cancelled by user")
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrEmpty()) {
                Log.e(LOGIN_OPTIONS_TAG, "Google sign-in returned empty ID token")
                Toast.makeText(context, "Google token missing", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            Log.d(LOGIN_OPTIONS_TAG, "Firebase auth request started")
            firebaseAuthWithGoogle(
                idToken = idToken,
                context = context,
                onSuccess = { uid, _ ->
                    Log.d(LOGIN_OPTIONS_TAG, "Google login success uid=$uid; restoring backup before Home")
                    restoreOrSyncAfterLogin(
                        context = context,
                        uid = uid,
                        setSyncing = { isGoogleSigningIn = it },
                        onComplete = { message, _ ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            editor.putBoolean("is_logged_in", true).apply()
                            navController.navigate(Home.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    )
                },
                onFailure = { error ->
                    Log.e(LOGIN_OPTIONS_TAG, "Firebase auth failed: $error")
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: ApiException) {
            Log.e(
                LOGIN_OPTIONS_TAG,
                "GoogleSignIn ApiException status=${e.statusCode} msg=${e.message}",
                e
            )
            val message = when (e.statusCode) {
                10 -> "Google Sign-In misconfigured (OAuth SHA / client). Please update Firebase config."
                12501 -> "Google Sign-In cancelled."
                else -> "Google Sign-In failed (code ${e.statusCode})"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(LOGIN_OPTIONS_TAG, "Unexpected GoogleSignIn exception", e)
            Toast.makeText(context, "Unexpected Google Sign-In error", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NexScene",
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        listOf(appColors().primary, Color(0xFFFFDEA8))
                    )
                ),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 6.sp
            )
            Icon(
                imageVector = Icons.Filled.LocalMovies,
                contentDescription = "Menu",
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Unlimited",
                fontSize = 48.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Cinematic",
                style = TextStyle(
                    brush = Brush.verticalGradient(
                        listOf(appColors().primary, Color(0xFFFFDEA8))
                    )
                ),
                fontSize = 60.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Journeys.",
                style = TextStyle(
                    brush = Brush.verticalGradient(
                        listOf(appColors().primary, Color(0xFFFFDEA8))
                    )
                ),
                fontSize = 60.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Track your favorites, discover new\nmasterpieces, and find where to\nstream them all in one place.",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal,
                lineHeight = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(52.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(appColors().primary, Color(0xFFFFDEA8))
                        )
                    )
                    .clickable(enabled = !isGoogleSigningIn) {
                        isGoogleSigningIn = true
                        googleSignInClient.signOut()
                            .addOnCompleteListener {
                                googleLauncher.launch(googleSignInClient.signInIntent)
                            }
                            .addOnFailureListener {
                                Log.e(LOGIN_OPTIONS_TAG, "Pre-login Google signOut failed; continuing", it)
                                googleLauncher.launch(googleSignInClient.signInIntent)
                            }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGoogleSigningIn) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Connecting...",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.google),
                            contentDescription = "Google"
                        )
                    }
                    Spacer(modifier = Modifier.size(14.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = buildTermsText(),
            color = Color.White.copy(alpha = 0.58f),
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            lineHeight = 25.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp)
        )
    }
}

private fun buildTermsText(): AnnotatedString {
    return buildAnnotatedString {
        append("By continuing, you agree to our\n")
        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
        append("Terms of Service")
        pop()
        append(" & ")
        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
        append("Privacy Policy")
        pop()
    }
}

private fun syncLocalDataToFirebase(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onComplete: (message: String, syncedAt: Long?) -> Unit
) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }
    val firestore = FirebaseFirestore.getInstance()
    val db = AppDataBase.getDatabase(context)
    val accountType = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?.lowercase()
        ?: "personal"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val states = db.titleStateDao().getAllStates()
            val modeDoc = firestore.collection("users").document(uid).collection("modes").document(accountType)
            val batch = firestore.batch()
            val syncedAt = System.currentTimeMillis()

            batch.set(
                modeDoc.collection("sync").document("meta"),
                mapOf("updatedAt" to syncedAt, "titleStateCount" to states.size),
                SetOptions.merge()
            )

            states.forEach { state ->
                val docId = "${state.itemId}_${state.mediaType}"
                batch.set(
                    modeDoc.collection("titleStates").document(docId),
                    mapOf(
                        "itemId" to state.itemId,
                        "mediaType" to state.mediaType,
                        "title" to state.title,
                        "posterUrl" to state.posterUrl,
                        "userRating" to state.userRating,
                        "inWatchlist" to state.inWatchlist,
                        "watched" to state.watched,
                        "updatedAt" to state.updatedAt
                    ),
                    SetOptions.merge()
                )
            }

            batch.commit()
                .addOnSuccessListener {
                    context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
                        .edit()
                        .putLong(LAST_SYNCED_AT_KEY, syncedAt)
                        .putString(ACTIVE_DATA_UID_KEY, uid)
                        .putString(ACTIVE_DATA_ACCOUNT_TYPE_KEY, accountType)
                        .apply()
                    mainHandler.post {
                        setSyncing(false)
                        onComplete("Sync completed successfully", syncedAt)
                    }
                }
                .addOnFailureListener { e ->
                    mainHandler.post {
                        setSyncing(false)
                        onComplete(e.message ?: "Sync failed", null)
                    }
                }
        } catch (e: Exception) {
            mainHandler.post {
                setSyncing(false)
                onComplete(e.message ?: "Sync failed", null)
            }
        }
    }
}

private fun restoreOrSyncAfterLogin(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onComplete: (message: String, syncedAt: Long?) -> Unit
) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }
    val firestore = FirebaseFirestore.getInstance()
    val db = AppDataBase.getDatabase(context)
    val accountType = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?.lowercase()
        ?: "personal"
    val userDoc = firestore.collection("users").document(uid)
    val preferredModeDoc = userDoc.collection("modes").document(accountType)

    fun applyRestore(states: List<TitleStateEntity>, resolvedMode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restoredAt = System.currentTimeMillis()
                states.forEach { db.titleStateDao().upsert(it) }
                context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
                    .edit()
                    .putLong(LAST_SYNCED_AT_KEY, restoredAt)
                    .putString(ACTIVE_DATA_UID_KEY, uid)
                    .putString(ACTIVE_DATA_ACCOUNT_TYPE_KEY, resolvedMode)
                    .putString("account_type", resolvedMode)
                    .apply()
                mainHandler.post {
                    setSyncing(false)
                    onComplete("Backup restored successfully (${states.size} items)", restoredAt)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    setSyncing(false)
                    onComplete(e.message ?: "Restore failed", null)
                }
            }
        }
    }

    fun parseStates(
        snap: com.google.firebase.firestore.QuerySnapshot
    ): List<TitleStateEntity> = snap.documents.mapNotNull { doc ->
        val itemId = doc.getLong("itemId")?.toInt() ?: 0
        if (itemId <= 0) return@mapNotNull null
        TitleStateEntity(
            itemId,
            doc.getString("mediaType").orEmpty().ifBlank { "movie" },
            doc.getString("title").orEmpty(),
            doc.getString("posterUrl"),
            doc.getLong("userRating")?.toInt() ?: 0,
            doc.getBoolean("inWatchlist") ?: false,
            doc.getBoolean("watched") ?: false,
            doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
    }

    preferredModeDoc.collection("titleStates").get()
        .addOnSuccessListener { preferredSnap ->
            if (!preferredSnap.isEmpty) {
                applyRestore(parseStates(preferredSnap), accountType)
                return@addOnSuccessListener
            }
            userDoc.collection("modes").get()
                .addOnSuccessListener { modesSnap ->
                    val modeDocs = modesSnap.documents
                    if (modeDocs.isEmpty()) {
                        syncLocalDataToFirebase(context, uid, setSyncing, onComplete)
                        return@addOnSuccessListener
                    }
                    tryAnyMode(
                        modeDocs = modeDocs,
                        context = context,
                        uid = uid,
                        setSyncing = setSyncing,
                        onComplete = onComplete,
                        applyRestore = { states, resolvedMode ->
                            applyRestore(states, resolvedMode)
                        }
                    )
                }
                .addOnFailureListener { e ->
                    mainHandler.post {
                        setSyncing(false)
                        onComplete(e.message ?: "Restore failed", null)
                    }
                }
        }
        .addOnFailureListener { e ->
            mainHandler.post {
                setSyncing(false)
                onComplete(e.message ?: "Restore failed", null)
            }
        }
}

private fun tryAnyMode(
    modeDocs: List<com.google.firebase.firestore.DocumentSnapshot>,
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onComplete: (message: String, syncedAt: Long?) -> Unit,
    applyRestore: (states: List<TitleStateEntity>, resolvedMode: String) -> Unit
) {
    val pending = AtomicInteger(modeDocs.size)
    var restored = false
    modeDocs.forEach { mode ->
        mode.reference.collection("titleStates").get()
            .addOnSuccessListener { snap ->
                if (!restored && !snap.isEmpty) {
                    restored = true
                    val states = snap.documents.mapNotNull { doc ->
                        val itemId = doc.getLong("itemId")?.toInt() ?: 0
                        if (itemId <= 0) return@mapNotNull null
                        TitleStateEntity(
                            itemId,
                            doc.getString("mediaType").orEmpty().ifBlank { "movie" },
                            doc.getString("title").orEmpty(),
                            doc.getString("posterUrl"),
                            doc.getLong("userRating")?.toInt() ?: 0,
                            doc.getBoolean("inWatchlist") ?: false,
                            doc.getBoolean("watched") ?: false,
                            doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                    }
                    applyRestore(states, mode.id.lowercase().ifBlank { "personal" })
                }
                if (pending.decrementAndGet() == 0 && !restored) {
                    syncLocalDataToFirebase(context, uid, setSyncing, onComplete)
                }
            }
            .addOnFailureListener {
                if (pending.decrementAndGet() == 0 && !restored) {
                    syncLocalDataToFirebase(context, uid, setSyncing, onComplete)
                }
            }
    }
}
