package com.mindsync.library.ui.dialog

import android.text.Editable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EditDescriptionViewModel : ViewModel() {
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description
    var submitListener: ((String) -> (Unit))? = null

    fun setDescription(text: String) {
        _description.value = text
    }

    fun onDescriptionChanged(inputDescription: Editable?) {
        _description.value = inputDescription.toString()
    }
}
