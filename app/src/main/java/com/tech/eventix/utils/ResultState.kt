package com.tech.eventix.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


sealed interface ResultState<out T> {
    data class Success<T>(val data: T) : ResultState<T>
    data class Error(val exception: Throwable? = null) : ResultState<Nothing> {
        fun getErrorMessage(): String {
            return exception?.message ?: "Unknown Error"
        }
    }
}

fun <T> Flow<T>.asResultState(): Flow<ResultState<T>> {
    return this
        .map<T, ResultState<T>> {
            ResultState.Success(it)
        }
        .catch {
            emit(ResultState.Error(it))
        }
}

internal fun <T> ResultState<T>.successOr(fallback: T): T {
    return (this as? ResultState.Success<T>)?.data ?: fallback
}

internal fun <T> ResultState<T>.succeeded(): Boolean {
    return this is ResultState.Success<T>
}
internal fun <T> ResultState<T>.error(): Boolean {
    return this is ResultState.Error
}

internal val <T> ResultState<T>.data: T?
    get() = (this as? ResultState.Success<T>)?.data

internal val <T> ResultState<T>.safeData: T?
    get() = (this as? ResultState.Success<T>)?.data

internal val <T> ResultState<T>.getOrThrowData: T
    get() = (this as ResultState.Success<T>).data

fun <T> T.isEmptyResponse(): Boolean {
    return this != null && this is List<*> && this.isEmpty()
}
