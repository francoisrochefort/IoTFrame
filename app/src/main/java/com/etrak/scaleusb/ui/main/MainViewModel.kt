package com.etrak.scaleusb.ui.main

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.etrak.scaleusb.domain.customer.CustomerRepository
import com.etrak.scaleusb.api.mc.Mc
import com.etrak.scaleusb.domain.scale.Scale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class MainViewModel(

    private val customerRepository: CustomerRepository,
    private val scale: Scale,
    private val savedStateHandle: SavedStateHandle

) : ViewModel() {

    private val scaleEvents = scale.events.shareIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly
    )

    val customers = customerRepository.listAll()
    var cabAngle by mutableStateOf(0)

    init {
        viewModelScope.launch {
            scaleEvents.collect { event ->
                when (event) {
                    is Scale.Event.OnCabAngle -> {
                        cabAngle = event.angle
                    }
                    else -> Unit
                }
            }
        }
    }

    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
    companion object {
        fun provideFactory(
            customerRepository: CustomerRepository,
            scale: Scale,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null,
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return MainViewModel(
                        customerRepository,
                        scale,
                        handle
                    ) as T
                }
            }
    }
}