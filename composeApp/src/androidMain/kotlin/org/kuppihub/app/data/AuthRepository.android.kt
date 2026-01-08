package org.kuppihub.app.data

// Use Native Android Imports for reliability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import org.kuppihub.app.model.KuppiUser

class AndroidAuthRepository : AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    override suspend fun sendEmailVerification(user: KuppiUser): Boolean {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun reloadUser(user: KuppiUser): KuppiUser? {
        return try {
            val firebaseUser = auth.currentUser
            firebaseUser?.reload()?.await() // Refresh data

            if (firebaseUser != null) {
                KuppiUser(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "", // Handle null safety
                    name = firebaseUser.displayName ?: "User",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    idToken = user.idToken, // Reuse the token we have
                    refreshToken = null,
                    isEmailVerified = firebaseUser.isEmailVerified
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signIn(email: String, pass: String): Result<KuppiUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user
            val token = user?.getIdToken(false)?.await()?.token ?: ""

            if (user != null) {
                val kuppiUser = KuppiUser(
                    id = user.uid,
                    email = user.email ?: "", // Fix: Convert String? to String
                    name = user.displayName ?: "User",
                    photoUrl = user.photoUrl?.toString(),
                    idToken = token,
                    refreshToken = null,
                    isEmailVerified = user.isEmailVerified
                )
                Result.success(kuppiUser)
            } else {
                Result.failure(Exception("User data missing"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, pass: String, name: String): Result<KuppiUser> {
        return try {
            // 1. Create User
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user

            if (user != null) {
                // 2. Update Name (Using Native Android Code)
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdates).await()
                } catch (e: Exception) {
                    println("Failed to set name: ${e.message}")
                }

                // 3. Get Token
                val token = user.getIdToken(false).await().token ?: ""

                val kuppiUser = KuppiUser(
                    id = user.uid,
                    email = user.email ?: "", // Fix: Convert String? to String
                    name = name, // Use the name we just set
                    photoUrl = user.photoUrl?.toString(),
                    idToken = token,
                    refreshToken = null,
                    isEmailVerified = user.isEmailVerified
                )
                Result.success(kuppiUser)
            } else {
                Result.failure(Exception("User data missing"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

actual fun getAuthRepository(): AuthRepository = AndroidAuthRepository()