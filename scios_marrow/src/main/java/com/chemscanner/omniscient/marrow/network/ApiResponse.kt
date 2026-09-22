package com.chemscanner.omniscient.marrow.network

sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
    object Empty : ApiResponse<Nothing>()
    
    /**
     * Helper function to convert network responses to ApiResponse
     */
    companion object {
        suspend fun <T> of(suspendFunc: suspend () -> T): ApiResponse<T> {
            return try {
                val result = suspendFunc()
                if (result != null) {
                    Success(result)
                } else {
                    Empty
                }
            } catch (e: Exception) {
                Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

/**
 * Extension function to transform ApiResponse
 */
fun <T, R> ApiResponse<T>.map(transform: (T) -> R): ApiResponse<R> {
    return when (this) {
        is ApiResponse.Success -> ApiResponse.Success(transform(data))
        is ApiResponse.Error -> this
        is ApiResponse.Loading -> this
        is ApiResponse.Empty -> this
    }
}