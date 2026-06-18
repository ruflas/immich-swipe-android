package com.example.immichswipe.core

import com.example.immichswipe.data.api.ImmichApi
import com.example.immichswipe.data.api.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {

    private var config: SessionConfig? = null

    var api: ImmichApi? = null
        private set

    // Flux global indiquant si le serveur est joignable.
    // Mis à jour automatiquement par l'intercepteur réseau.
    private val _isServerReachable = MutableStateFlow(true)
    val isServerReachable = _isServerReachable.asStateFlow()

    fun updateReachability(reachable: Boolean) {
        _isServerReachable.value = reachable
    }

    fun initialize(config: SessionConfig) {
        this.config = config
        this.api = RetrofitFactory.create(config)
    }

    fun clear() {
        config = null
        api = null
    }

    fun isLoggedIn(): Boolean {
        return api != null
    }

    fun getBaseUrl(): String? = config?.baseUrl

    fun getApiKey(): String? = config?.apiKey
}