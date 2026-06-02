package com.safc.caficultura.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.safc.caficultura.AppDependencies

class SafcViewModelFactory(
    private val deps: AppDependencies
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(deps) as T

            modelClass.isAssignableFrom(EmpleadosViewModel::class.java) ->
                EmpleadosViewModel(deps) as T

            modelClass.isAssignableFrom(ProduccionViewModel::class.java) ->
                ProduccionViewModel(deps) as T

            modelClass.isAssignableFrom(PagosViewModel::class.java) ->
                PagosViewModel(deps) as T

            modelClass.isAssignableFrom(ReportesViewModel::class.java) ->
                ReportesViewModel(deps) as T

            modelClass.isAssignableFrom(MenuViewModel::class.java) ->
                MenuViewModel(deps) as T

            else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
        }
    }
}
