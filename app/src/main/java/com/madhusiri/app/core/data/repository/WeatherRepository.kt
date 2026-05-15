package com.madhusiri.app.core.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherInfo(
    val windSpeedKmh: Float,
    val windDirection: String,
    val temperatureCelsius: Float,
    val isHighWindRisk: Boolean
)

@Singleton
class WeatherRepository @Inject constructor() {

    // Mock implementation of weather fetching
    // In production, this would use Retrofit to call OpenWeather API
    fun getCurrentWeatherStream(): Flow<WeatherInfo> = flow {
        // Emit an initial loading state or safe weather
        emit(
            WeatherInfo(
                windSpeedKmh = 5.0f,
                windDirection = "NW",
                temperatureCelsius = 22.0f,
                isHighWindRisk = false
            )
        )
        
        // Simulate network delay
        delay(2000)

        // Simulate high wind conditions to demonstrate the warning
        emit(
            WeatherInfo(
                windSpeedKmh = 18.5f,
                windDirection = "N",
                temperatureCelsius = 24.5f,
                isHighWindRisk = true // Wind > 15km/h
            )
        )
    }
}
