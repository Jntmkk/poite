package net.coolshell.poite.shared.core.network

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.SerializationException

/** 统一异步结果。Error 里放类型化 AppError，禁止裸 String 透传给 UI */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val error: AppError) : Resource<Nothing>()
}

sealed class AppError {
    data class Network(val cause: String? = null) : AppError()
    data class Http(val code: Int) : AppError()
    data class Parse(val cause: String? = null) : AppError()
    data class Unknown(val cause: String? = null) : AppError()
}

suspend fun <T> safeApiCall(block: suspend () -> T): Resource<T> = try {
    Resource.Success(block())
} catch (e: ResponseException) {
    Resource.Error(AppError.Http(e.response.status.value))
} catch (e: JsonConvertException) {
    Resource.Error(AppError.Parse(e.message))
} catch (e: SerializationException) {
    Resource.Error(AppError.Parse(e.message))
} catch (e: HttpRequestTimeoutException) {
    Resource.Error(AppError.Network(e.message))
} catch (e: IOException) {
    Resource.Error(AppError.Network(e.message))
} catch (e: Exception) {
    Resource.Error(AppError.Unknown(e.message))
}
