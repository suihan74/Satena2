package com.suihan74.satena2.utility.exception

/**
 * 追加しようとした要素が既に存在する
 */
class AlreadyExistsException(
    message: String? = null,
    cause: Throwable? = null
) : Throwable(message, cause)
