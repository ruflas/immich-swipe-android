package com.example.immichswipe.data.api

import com.example.immichswipe.core.SessionConfig
import com.example.immichswipe.core.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitFactory {
    fun create(config: SessionConfig): ImmichApi {
        // Intercepteur pour logger les requêtes
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        // Intercepteur pour ajouter la clé API
        val apiKeyInterceptor = Interceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader("x-api-key", config.apiKey)
                .build()
            chain.proceed(request)
        }

        // SOLUTION : Intercepteur global de connectivité.
        // Il intercepte TOUTES les requêtes réseau et met à jour l'état de la pastille.
        val connectivityInterceptor = Interceptor { chain ->
            try {
                val response = chain.proceed(chain.request())
                // Si on a une réponse (même une erreur 4xx ou 5xx), le serveur a répondu.
                SessionManager.updateReachability(true)
                response
            } catch (e: IOException) {
                // Si on a une exception réseau (timeout, pas d'internet, etc.), le serveur est injoignable.
                SessionManager.updateReachability(false)
                throw e
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(connectivityInterceptor)
            .addInterceptor(logging)
            .build()

        // Configure Retrofit avec l'URL de base, le client HTTP et le convertisseur JSON (Gson).
        val retrofit = Retrofit.Builder()
            .baseUrl(config.baseUrl) // 🔥 DYNAMIQUE
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ImmichApi::class.java)
    }

}