package com.brewkery.app.data.repository

import com.brewkery.app.data.model.MenuItem
import com.brewkery.app.data.model.MenuResponse
import com.brewkery.app.data.remote.BrewkeryApi
import com.google.gson.JsonParseException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class MenuRepository(private val api: BrewkeryApi) {

    private var cachedMenu: MenuResponse? = null

    suspend fun getMenu(forceRefresh: Boolean = false): Result<MenuResponse> {
        if (!forceRefresh) cachedMenu?.let { return Result.success(it) }
        return safeCall { api.getMenu() }.onSuccess { cachedMenu = it }
    }

    /** Uses the single-item endpoint; falls back to the cached menu entry if it fails. */
    suspend fun getItem(id: Int): Result<MenuItem> {
        val remote = safeCall { api.getItem(id) }
            .mapCatching { item -> if (item.name.isNullOrBlank()) error("Empty item") else item }
        if (remote.isSuccess) return remote
        val cached = cachedMenu?.items.orEmpty().firstOrNull { it.id == id }
        return if (cached != null) Result.success(cached) else remote
    }

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> "The server returned an error (${code()}). Please try again."
    is JsonParseException -> "We received unexpected data from the server."
    is IOException -> "Can't reach Brewkery. Check your internet connection and try again."
    else -> "Something went wrong. Please try again."
}

/** Simple manual DI — keeps the project small without a DI framework. */
object AppContainer {
    val repository: MenuRepository by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BrewkeryApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        MenuRepository(retrofit.create(BrewkeryApi::class.java))
    }
}
