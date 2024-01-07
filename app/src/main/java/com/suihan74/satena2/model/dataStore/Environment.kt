package com.suihan74.satena2.model.dataStore

import android.content.Context
import androidx.datastore.dataStore
import kotlinx.serialization.Serializable

val Context.environmentDataStore by dataStore(
    fileName = "environment",
    serializer = jsonDataStoreSerializer(defaultValue = { Environment() })
)

// ------ //

@Serializable
data class Environment(
    /** 固有ID */
    val uuid: String? = null
)
