
package com.example.studybetter.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.18.13:5293/"

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->

                val prefs = appContext.getSharedPreferences(
                    "study_better_settings",
                    Context.MODE_PRIVATE
                )

                val token = prefs.getString("token", null)

                val request = chain.request().newBuilder()

                if (!token.isNullOrBlank()) {
                    request.addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }

                chain.proceed(request.build())
            }
            .build()
    }

    val api: StudyBetterApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StudyBetterApi::class.java)
    }
}
