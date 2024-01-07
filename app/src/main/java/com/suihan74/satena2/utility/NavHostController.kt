package com.suihan74.satena2.utility

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Suppress("UNCHECKED_CAST", "DEPRECATION")
fun <T> NavController.currentArgument(key: String) : T? {
    return currentBackStackEntry?.arguments?.get(key) as? T
}

@Suppress("UNCHECKED_CAST", "DEPRECATION")
fun <T> NavBackStackEntry.argument(key: String) : T? {
    return arguments?.get(key) as? T
}
