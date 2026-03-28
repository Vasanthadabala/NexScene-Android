package com.piggylabs.nexscene.ui.screens.settings

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
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
import com.piggylabs.nexscene.navigation.OnBoarding
import com.piggylabs.nexscene.navigation.Profile
import com.piggylabs.nexscene.navigation.components.BottomBar
import com.piggylabs.nexscene.ui.theme.appColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

private val ScreenBackground = Color(0xFF120B02)
private val AccentYellow = Color(0xFFFFC107)
private val SoftText = Color(0xFFCDBFA8)
private val MutedText = Color(0xFF8B7A5E)
private const val LAST_SYNCED_AT_KEY = "last_synced_at"
private const val ACTIVE_DATA_UID_KEY = "active_data_uid"
private const val ACTIVE_DATA_ACCOUNT_TYPE_KEY = "active_data_account_type"

@ExperimentalMaterial3Api
@Composable
fun SettingsScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().neutral)
                .padding(innerPadding)
        ) {
            SettingsScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SettingsScreenComponent(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("MY_PRE", android.content.Context.MODE_PRIVATE)
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

    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val userName = prefs.getString("userName", firebaseUser?.displayName ?: "Julian Vane") ?: "Julian Vane"
    var isSignedIn by remember { mutableStateOf(firebaseUser != null) }
    var userEmail by remember { mutableStateOf(firebaseUser?.email ?: "Not connected") }
    val userPhotoUrl = firebaseUser?.photoUrl?.toString()
    var isSyncing by remember { mutableStateOf(false) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var lastSyncedAt by remember { mutableStateOf(prefs.getLong(LAST_SYNCED_AT_KEY, 0L)) }
    var autoMarkWatched by remember { mutableStateOf(prefs.getBoolean("auto_mark_watched", true)) }
    var showWatchedBadge by remember { mutableStateOf(prefs.getBoolean("show_watched_badge", true)) }
    var releaseReminders by remember { mutableStateOf(prefs.getBoolean("release_reminders", false)) }

    val logout: () -> Unit = {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut()

        prefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("uid")
            .remove("userName")
            .apply()
        isSignedIn = false
        userEmail = "Not connected"

        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
        navController.navigate(OnBoarding.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleSigningIn = false

        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrBlank()) {
                Toast.makeText(context, "Google token missing", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            firebaseAuthWithGoogle(
                idToken = idToken,
                context = context,
                onSuccess = { uid, _ ->
                    isSignedIn = true
                    userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Connected"
                    restoreOrSyncAfterLogin(
                        context = context,
                        uid = uid,
                        setSyncing = { isSyncing = it },
                        onComplete = { message, timestamp ->
                            timestamp?.let { lastSyncedAt = it }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onFailure = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In failed (code ${e.statusCode})", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unexpected Google Sign-In error", Toast.LENGTH_LONG).show()
        }
    }

    val restoreGoogleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isGoogleSigningIn = false

        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrBlank()) {
                Toast.makeText(context, "Google token missing", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            firebaseAuthWithGoogle(
                idToken = idToken,
                context = context,
                onSuccess = { uid, _ ->
                    isSignedIn = true
                    userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Connected"
                    restoreOrSyncAfterLogin(
                        context = context,
                        uid = uid,
                        setSyncing = { isSyncing = it },
                        onComplete = { message, timestamp ->
                            timestamp?.let { lastSyncedAt = it }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                onFailure = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In failed (code ${e.statusCode})", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unexpected Google Sign-In error", Toast.LENGTH_LONG).show()
        }
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete account?") },
            text = {
                Text("This permanently deletes your Firebase account and synced cloud data.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccount(
                            context = context,
                            onProgress = { syncing -> isSyncing = syncing },
                            onComplete = { message, deleted ->
                                if (deleted) {
                                    prefs.edit()
                                        .putBoolean("is_logged_in", false)
                                        .remove("uid")
                                        .remove("userName")
                                        .remove(LAST_SYNCED_AT_KEY)
                                        .remove(ACTIVE_DATA_UID_KEY)
                                        .remove(ACTIVE_DATA_ACCOUNT_TYPE_KEY)
                                        .apply()

                                    navController.navigate(OnBoarding.route) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF8A80))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {

        Text(
            text = "Settings",
            color = appColors().primary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable{
                    navController.navigate(Profile.route)
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = appColors().cards),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2E5E95))
                        .border(2.dp, AccentYellow, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (!userPhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color(0xFFE4EEF8),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(42.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(5.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(AccentYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Badge",
                            tint = Color.Black,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 32.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Premium Member since",
                        color = SoftText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "2022",
                        color = SoftText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        SectionHeader("ACCOUNT")
        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Person,
                title = "Google Account",
                subtitle = userEmail,
                iconTint = AccentYellow,
                onClick = {
                    if (isSignedIn || isGoogleSigningIn || isSyncing) return@SettingsRow
                    isGoogleSigningIn = true
                    googleSignInClient.signOut()
                        .addOnCompleteListener { googleLauncher.launch(googleSignInClient.signInIntent) }
                        .addOnFailureListener { googleLauncher.launch(googleSignInClient.signInIntent) }
                }
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            SettingsRow(
                icon = Icons.Default.Sync,
                title = "Sync Now",
                subtitle = if (!isSignedIn) "Connect Google account first" else if (isSyncing) "Syncing local tracker data..." else formatLastSynced(lastSyncedAt),
                iconTint = AccentYellow,
                onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid.isNullOrEmpty()) {
                        Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show()
                        return@SettingsRow
                    }
                    syncLocalDataToFirebase(
                        context = context,
                        uid = uid,
                        setSyncing = { syncing -> isSyncing = syncing },
                        onComplete = { message, timestamp ->
                            timestamp?.let { lastSyncedAt = it }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )

//            Divider(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp),
//                color = Color.White.copy(alpha = 0.1f),
//                thickness = 1.dp
//            )
//
//            SettingsRow(
//                icon = Icons.Default.Sync,
//                title = "Restore Backup",
//                subtitle = if (!isSignedIn) "Sign in and fetch cloud backup" else "Re-auth and restore from cloud",
//                iconTint = AccentYellow,
//                onClick = {
//                    if (isGoogleSigningIn || isSyncing) return@SettingsRow
//                    isGoogleSigningIn = true
//                    googleSignInClient.signOut()
//                        .addOnCompleteListener { restoreGoogleLauncher.launch(googleSignInClient.signInIntent) }
//                        .addOnFailureListener { restoreGoogleLauncher.launch(googleSignInClient.signInIntent) }
//                }
//            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            SettingsRow(
                icon = Icons.Default.DeleteOutline,
                title = "Delete Account",
                subtitle = "Delete Firebase account and cloud backup data",
                iconTint = Color(0xFFFF8A80),
                onClick = { showDeleteAccountDialog = true }
            )
        }

        SectionHeader("TRACKING")
        SettingsGroup {
            SettingsToggleRow(
                icon = Icons.Default.Visibility,
                title = "Auto-mark watched after rating",
                checked = autoMarkWatched,
                onCheckedChange = {
                    autoMarkWatched = it
                    prefs.edit().putBoolean("auto_mark_watched", it).apply()
                }
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            SettingsToggleRow(
                icon = Icons.Default.Bookmark,
                title = "Show watched badges in lists",
                checked = showWatchedBadge,
                onCheckedChange = {
                    showWatchedBadge = it
                    prefs.edit().putBoolean("show_watched_badge", it).apply()
                }
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = "New release reminders",
                checked = releaseReminders,
                onCheckedChange = {
                    releaseReminders = it
                    prefs.edit().putBoolean("release_reminders", it).apply()
                }
            )
        }

        SectionHeader("DATA")
        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Inventory2,
                title = "Export Watchlist",
                subtitle = "CSV export (coming soon)",
                iconTint = AccentYellow.copy(alpha = 0.7f),
                onClick = {}
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            SettingsRow(
                icon = Icons.Default.DeleteOutline,
                title = "Reset watched & wishlist data",
                subtitle = "Clears local tracking data on this device",
                iconTint = Color(0xFFFF8A80),
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        AppDataBase.getDatabase(context).titleStateDao().clearAllStates()
                    }
                    Toast.makeText(context, "Local tracking data cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }

        SectionHeader("ABOUT")
        SettingsGroup {
            SettingsRow(
                icon = Icons.Default.Help,
                title = "Help Center",
                iconTint = AccentYellow,
                trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                onClick = {}
            )

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            SettingsRow(
                icon = Icons.Default.Gavel,
                title = "Terms of Service",
                iconTint = AccentYellow,
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "NexScene V1.0.1",
            color = MutedText,
            fontSize = 12.sp,
            letterSpacing = 1.6.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = logout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFFC2B9)
            ),
            border = BorderStroke(1.dp, Color(0xFF5A4731)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Log Out",
                tint = Color(0xFFFFC2B9),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatLastSynced(timestamp: Long): String {
    if (timestamp <= 0L) return "Not synced yet"
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return "Last synced ${formatter.format(Date(timestamp))}"
}

private fun syncLocalDataToFirebase(
    context: android.content.Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onComplete: (message: String, syncedAt: Long?) -> Unit
) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }
    val firestore = FirebaseFirestore.getInstance()
    val db = AppDataBase.getDatabase(context)
    val accountType = context.getSharedPreferences("MY_PRE", android.content.Context.MODE_PRIVATE)
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
                mapOf(
                    "updatedAt" to syncedAt,
                    "titleStateCount" to states.size
                ),
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
                    context.getSharedPreferences("MY_PRE", android.content.Context.MODE_PRIVATE)
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
    context: android.content.Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onComplete: (message: String, syncedAt: Long?) -> Unit
) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }
    val firestore = FirebaseFirestore.getInstance()
    val db = AppDataBase.getDatabase(context)
    val accountType = context.getSharedPreferences("MY_PRE", android.content.Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?.lowercase()
        ?: "personal"
    val userDoc = firestore.collection("users").document(uid)
    val preferredModeDoc = userDoc.collection("modes").document(accountType)

    fun applyRestoreFromSnapshot(
        snap: com.google.firebase.firestore.QuerySnapshot,
        resolvedMode: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restoredAt = System.currentTimeMillis()
                val remoteStates = snap.documents.mapNotNull { doc ->
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
                        doc.getLong("updatedAt") ?: restoredAt
                    )
                }

                remoteStates.forEach { db.titleStateDao().upsert(it) }

                context.getSharedPreferences("MY_PRE", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putLong(LAST_SYNCED_AT_KEY, restoredAt)
                    .putString(ACTIVE_DATA_UID_KEY, uid)
                    .putString(ACTIVE_DATA_ACCOUNT_TYPE_KEY, resolvedMode)
                    .putString("account_type", resolvedMode)
                    .apply()

                mainHandler.post {
                    setSyncing(false)
                    onComplete("Backup restored successfully (${remoteStates.size} items)", restoredAt)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    setSyncing(false)
                    onComplete(e.message ?: "Restore failed", null)
                }
            }
        }
    }

    preferredModeDoc.collection("titleStates").get()
        .addOnSuccessListener { preferredSnap ->
            if (!preferredSnap.isEmpty) {
                applyRestoreFromSnapshot(preferredSnap, accountType)
                return@addOnSuccessListener
            }

            // Fallback: restore from any mode that has backup data.
            userDoc.collection("modes").get()
                .addOnSuccessListener { modesSnap ->
                    val modeDocs = modesSnap.documents
                    if (modeDocs.isEmpty()) {
                        syncLocalDataToFirebase(context, uid, setSyncing, onComplete)
                        return@addOnSuccessListener
                    }

                    val pending = AtomicInteger(modeDocs.size)
                    var restored = false
                    var failed = false

                    modeDocs.forEach { mode ->
                        mode.reference.collection("titleStates").get()
                            .addOnSuccessListener { candidateSnap ->
                                if (!restored && !candidateSnap.isEmpty) {
                                    restored = true
                                    applyRestoreFromSnapshot(
                                        candidateSnap,
                                        mode.id.lowercase().ifBlank { accountType }
                                    )
                                }
                                if (pending.decrementAndGet() == 0 && !restored && !failed) {
                                    syncLocalDataToFirebase(context, uid, setSyncing, onComplete)
                                }
                            }
                            .addOnFailureListener {
                                if (pending.decrementAndGet() == 0 && !restored && !failed) {
                                    failed = true
                                    syncLocalDataToFirebase(context, uid, setSyncing, onComplete)
                                }
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
        .addOnFailureListener { e ->
            mainHandler.post {
                setSyncing(false)
                onComplete(e.message ?: "Restore failed", null)
            }
        }
}

private fun deleteAccount(
    context: android.content.Context,
    onProgress: (Boolean) -> Unit,
    onComplete: (message: String, deleted: Boolean) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onComplete("No signed-in account found", false)
        return
    }

    onProgress(true)
    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(user.uid)
    val chunkSize = 400L
    val mainHandler = Handler(Looper.getMainLooper())
    val accountType = context.getSharedPreferences("MY_PRE", android.content.Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?.lowercase()
        ?: "personal"
    val preferredModeDoc = userDoc.collection("modes").document(accountType)

    fun finish(message: String, deleted: Boolean) {
        onProgress(false)
        onComplete(message, deleted)
    }

    fun clearLocalDatabase(onDone: (Exception?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDataBase.getDatabase(context).titleStateDao().clearAllStates()
                mainHandler.post { onDone(null) }
            } catch (e: Exception) {
                mainHandler.post { onDone(e) }
            }
        }
    }

    fun deleteAuthUser() {
        user.delete()
            .addOnSuccessListener {
                FirebaseAuth.getInstance().signOut()
                clearLocalDatabase { localError ->
                    if (localError != null) {
                        finish("Account deleted, but failed to clear local data", true)
                    } else {
                        finish("Account and local data deleted successfully", true)
                    }
                }
            }
            .addOnFailureListener { e ->
                val message = if ((e.message ?: "").contains("recent", ignoreCase = true)) {
                    "Please log in again and retry account deletion."
                } else {
                    e.message ?: "Account deletion failed"
                }
                finish(message, false)
            }
    }

    fun deleteCollectionInChunks(
        collection: com.google.firebase.firestore.CollectionReference,
        onDone: (Exception?) -> Unit
    ) {
        collection.limit(chunkSize).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onDone(null)
                    return@addOnSuccessListener
                }
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener { deleteCollectionInChunks(collection, onDone) }
                    .addOnFailureListener { e -> onDone(e) }
            }
            .addOnFailureListener { e -> onDone(e) }
    }

    fun deleteCollectionsUnderDoc(
        docRef: com.google.firebase.firestore.DocumentReference,
        names: List<String>,
        index: Int = 0,
        onDone: (Exception?) -> Unit
    ) {
        if (index >= names.size) {
            onDone(null)
            return
        }
        deleteCollectionInChunks(docRef.collection(names[index])) { err ->
            if (err != null) {
                onDone(err)
                return@deleteCollectionInChunks
            }
            deleteCollectionsUnderDoc(docRef, names, index + 1, onDone)
        }
    }

    val knownModeCollections = listOf(
        "titleStates",
        "sync",
        "categories",
        "expenses",
        "incomes",
        "subscriptions",
        "businessParties",
        "businessEntries"
    )

    fun deleteModeDocWithChildren(
        modeDoc: com.google.firebase.firestore.DocumentReference,
        onDone: (Exception?) -> Unit
    ) {
        deleteCollectionsUnderDoc(modeDoc, knownModeCollections) { err ->
            if (err != null) {
                onDone(err)
                return@deleteCollectionsUnderDoc
            }
            modeDoc.delete()
                .addOnSuccessListener { onDone(null) }
                .addOnFailureListener { e -> onDone(e) }
        }
    }

    fun deleteAllModes(onDone: (Exception?) -> Unit) {
        userDoc.collection("modes").limit(chunkSize).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onDone(null)
                    return@addOnSuccessListener
                }
                val modeRefs = snapshot.documents.map { it.reference }
                fun next(index: Int) {
                    if (index >= modeRefs.size) {
                        deleteAllModes(onDone)
                        return
                    }
                    deleteModeDocWithChildren(modeRefs[index]) { err ->
                        if (err != null) {
                            onDone(err)
                            return@deleteModeDocWithChildren
                        }
                        next(index + 1)
                    }
                }
                next(0)
            }
            .addOnFailureListener { e -> onDone(e) }
    }

    fun deleteLegacyUserCollections(onDone: (Exception?) -> Unit) {
        val userLevelLegacy = listOf(
            "titleStates",
            "sync",
            "categories",
            "expenses",
            "incomes",
            "subscriptions",
            "businessParties",
            "businessEntries"
        )
        deleteCollectionsUnderDoc(userDoc, userLevelLegacy, onDone = onDone)
    }

    deleteModeDocWithChildren(preferredModeDoc) { preferredModeErr ->
        if (preferredModeErr != null) {
            finish(preferredModeErr.message ?: "Failed deleting cloud data", false)
            return@deleteModeDocWithChildren
        }
        deleteAllModes { allModesErr ->
            if (allModesErr != null) {
                finish(allModesErr.message ?: "Failed deleting cloud data", false)
                return@deleteAllModes
            }
            deleteLegacyUserCollections { legacyErr ->
                if (legacyErr != null) {
                    finish(legacyErr.message ?: "Failed deleting cloud data", false)
                    return@deleteLegacyUserCollections
                }
                userDoc.delete()
                    .addOnSuccessListener { deleteAuthUser() }
                    .addOnFailureListener { e ->
                        finish(e.message ?: "Failed to delete cloud profile", false)
                    }
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Spacer(modifier = Modifier.height(22.dp))
    Text(
        text = label,
        color = SoftText,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.5.sp,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors().cards
        ),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = AccentYellow,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    color = SoftText,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = Color(0xFF66553F)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AccentYellow,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                uncheckedBorderColor = Color.White.copy(alpha = 0.18f),
                checkedThumbColor = Color(0xFF1B1308),
                checkedTrackColor = AccentYellow,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.18f)
            )
        )
    }
}
