package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.kuppihub.app.data.ApiConstants
import org.kuppihub.app.data.getDeviceType

@Serializable
data class GitHubAsset(
    val name: String,
    val browser_download_url: String
)

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val html_url: String,
    val body: String,
    val assets: List<GitHubAsset> = emptyList()
)

class UpdateViewModel : ViewModel() {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val _updateInfo = MutableStateFlow<GitHubRelease?>(null)
    val updateInfo = _updateInfo.asStateFlow()

    private val _downloadUrl = MutableStateFlow<String?>(null)
    val downloadUrl = _downloadUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun checkForUpdates(currentVersion: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val release: GitHubRelease = client.get(ApiConstants.GITHUB_LATEST_RELEASE).body()
                
                val latestTag = release.tag_name.replace("v", "").trim()
                val currentTag = currentVersion.replace("v", "").trim()

                if (latestTag != currentTag) {
                    _updateInfo.value = release
                    _downloadUrl.value = findBestAsset(release.assets, release.html_url)
                } else {
                    _updateInfo.value = null
                    _error.value = "no_update"
                }
            } catch (e: Exception) {
                _error.value = "Failed to check for updates"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun findBestAsset(assets: List<GitHubAsset>, fallbackUrl: String): String {
        val platform = getDeviceType() // "android", "desktop", etc.
        
        if (platform == "android") {
            return assets.find { it.name.contains("-release.apk", ignoreCase = true) }?.browser_download_url ?: fallbackUrl
        }
        
        if (platform == "desktop") {
            // Try to detect OS from JVM system properties if on desktop
            val osName = try { System.getProperty("os.name").lowercase() } catch(e: Exception) { "" }
            return when {
                osName.contains("mac") -> assets.find { it.name.endsWith(".dmg") }?.browser_download_url
                osName.contains("win") -> assets.find { it.name.endsWith(".msi") }?.browser_download_url
                osName.contains("nux") || osName.contains("nix") -> assets.find { it.name.endsWith(".deb") }?.browser_download_url
                else -> null
            } ?: fallbackUrl
        }

        return fallbackUrl
    }
    
    fun clearUpdateInfo() {
        _updateInfo.value = null
        _error.value = null
        _downloadUrl.value = null
    }
}
