package com.suihan74.satena2.utility.extension

/**
 * 引数で渡された要素をすべて追加する
 */
fun <T> MutableList<T>.add(vararg args: T) : Boolean = addAll(args)
