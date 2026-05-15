package com.madhusiri.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madhusiri.app.core.data.repository.WeatherInfo
import com.madhusiri.app.core.data.repository.WeatherRepository
import com.madhusiri.app.domain.repository.HiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val hiveRepository: HiveRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val averageHealthScore: StateFlow<Double> = hiveRepository.getAverageHealthScore()
        .map { it ?: 0.0 }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val weatherState: StateFlow<WeatherInfo?> = weatherRepository.getCurrentWeatherStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
