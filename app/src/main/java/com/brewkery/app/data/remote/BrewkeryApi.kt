package com.brewkery.app.data.remote

import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.data.model.MenuResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApi {
    @GET("data.json")
    suspend fun getMenu(): MenuResponse

    @GET("api/items/{id}.json")
    suspend fun getItem(@Path("id") id: Int): MenuItem

    companion object {
        const val BASE_URL = "https://vivekshah138.github.io/Brewkery/"
    }
}
