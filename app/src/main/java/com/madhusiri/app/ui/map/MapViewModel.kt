package com.madhusiri.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madhusiri.app.data.local.entity.HiveEntity
import com.madhusiri.app.domain.repository.HiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: HiveRepository
) : ViewModel() {

    val allHives: StateFlow<List<HiveEntity>> = repository.getAllHives().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertHive(hive: HiveEntity) {
        viewModelScope.launch {
            repository.insertHive(hive)
        }
    }
}
