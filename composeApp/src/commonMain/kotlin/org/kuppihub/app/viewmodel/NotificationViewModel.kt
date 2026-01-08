package org.kuppihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kuppihub.app.data.NotificationItem
import org.kuppihub.app.data.NotificationRepository

class NotificationViewModel : ViewModel() {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }
    
    private val repository = NotificationRepository(client)

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadNotifications(idToken: String) {
        if (idToken.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getNotifications(1, idToken)
                _notifications.value = list
            } catch (e: Exception) {
                println("Failed to load notifications: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notification: NotificationItem, idToken: String) {
        if (notification.is_read) return
        
        viewModelScope.launch {
            val success = repository.markAsRead(notification.id, idToken)
            if (success) {
                _notifications.value = _notifications.value.map {
                    if (it.id == notification.id) it.copy(is_read = true) else it
                }
            }
        }
    }
}
