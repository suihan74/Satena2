package com.suihan74.satena2.utility

import android.content.Context
import androidx.lifecycle.ViewModel
import com.suihan74.satena2.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class ViewModel @Inject constructor() : ViewModel() {
    @Inject
    lateinit var application : Application

    protected val context : Context
        get() = application
}
