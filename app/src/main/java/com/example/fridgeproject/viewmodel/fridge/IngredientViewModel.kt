package com.example.fridgeproject.viewmodel.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.domain.IngredientRepository

class IngredientViewModel(
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

//    fun migrate() {
//        viewModelScope.launch {
//            ingredientRepository.uploadCatalog()
//        }
//    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                IngredientViewModel(
                    ingredientRepository = app.container.ingredientRepository
                )
            }
        }
    }
}