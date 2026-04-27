package com.example.strivn.data.repository

import com.example.strivn.network.RetrofitClient
import com.example.strivn.network.StrivnApiService

/**
 * Default [TrainingRepository] for the app ([ApiRepository] + Retrofit). Swap here for tests; [FakeRepository]
 * is legacy static data only and is not wired here.
 */
object TrainingRepositoryProvider {

    private val api: StrivnApiService by lazy {
        RetrofitClient.retrofit.create(StrivnApiService::class.java)
    }

    val instance: TrainingRepository by lazy { ApiRepository(api) }
}
