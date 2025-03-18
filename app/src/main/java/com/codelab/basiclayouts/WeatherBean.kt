package com.codelab.basiclayouts


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import retrofit2.HttpException
import java.io.IOException
import java.lang.reflect.Type

class WeatherBeanDeserializer : JsonDeserializer<WeatherBean> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): WeatherBean {
        val jsonObject = json.asJsonObject

        // 解析 lives 字段
        val livesArray = jsonObject.getAsJsonArray("lives")
        val lives = mutableListOf<Live>()
        for (element in livesArray) {
            val liveObject = element.asJsonObject
            val live = Live(
                province = liveObject.get("province").asString,
                city = liveObject.get("city").asString,
                adcode = liveObject.get("adcode").asString,
                weather = liveObject.get("weather").asString,
                temperature = liveObject.get("temperature").asString,
                winddirection = liveObject.get("winddirection").asString,
                windpower = liveObject.get("windpower").asString,
                humidity = liveObject.get("humidity").asString,
                reporttime = liveObject.get("reporttime").asString,
                temperature_float = liveObject.get("temperature_float").asString,
                humidity_float = liveObject.get("humidity_float").asString
            )
            lives.add(live)
        }

        return WeatherBean(
            status = jsonObject.get("status").asString,
            count = jsonObject.get("count").asString,
            info = jsonObject.get("info").asString,
            infocode = jsonObject.get("infocode").asString,
            lives = lives
        )
    }
}
interface WeatherApiService {
    @GET("v3/weather/weatherInfo")
    suspend fun getWeather(
        @Query("city") city: String, // 城市编码
        @Query("key") apiKey: String // API Key
    ): WeatherBean
}
val gson = GsonBuilder()
    .registerTypeAdapter(WeatherBean::class.java, WeatherBeanDeserializer())
    .create()

val retrofit = Retrofit.Builder()
    .baseUrl("https://restapi.amap.com/")
    .addConverterFactory(GsonConverterFactory.create(gson))
    .build()

val weatherApiService = retrofit.create(WeatherApiService::class.java)




//val retrofit = Retrofit.Builder()
//    .baseUrl("https://restapi.amap.com/") // 高德地图 API 基础 URL
//    .addConverterFactory(GsonConverterFactory.create())
//    .build()
//
//val weatherApiService = retrofit.create(WeatherApiService::class.java)

class WeatherViewModel : ViewModel() {
    private val _weatherBean = MutableLiveData<WeatherBean>()
    val weatherBean: LiveData<WeatherBean> get() = _weatherBean

    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                // 检查网络连接
//                val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                val network = connectivityManager.activeNetwork
//                val capabilities = connectivityManager.getNetworkCapabilities(network)
//                if (capabilities == null || !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
//                    Log.e("WeatherViewModel", "No internet connection")
//                    return@launch
//                }

                // 打印生成的 URL
                val url = "https://restapi.amap.com/v3/weather/weatherInfo?city=$city&key=$apiKey"
                Log.d("WeatherViewModel", "Generated URL: $url")

                // 调用 API
                val response = weatherApiService.getWeather(city, apiKey)
                _weatherBean.value = response
            } catch (e: IOException) {
                Log.e("WeatherViewModel", "Network error: ${e.message}")
            } catch (e: HttpException) {
                Log.e("WeatherViewModel", "HTTP error: ${e.code()}, ${e.message()}")
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("WeatherViewModel", "Error response: $errorBody")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Unknown error: ${e.message}")
            }
        }
    }




}

data class WeatherBean(
    val status: String,
    val count: String,
    val info: String,
    val infocode: String,
    val lives: List<Live>
)

data class Live(
    val province: String,
    val city: String,
    val adcode: String,
    val weather: String,
    val temperature: String,
    val winddirection: String,
    val windpower: String,
    val humidity: String,
    val reporttime: String,
    val temperature_float: String,
    val humidity_float: String
)
fun convertToWeatherData(weatherBean: WeatherBean): WeatherData {
    // 检查 lives 是否为空
    if (weatherBean.lives.isEmpty()) {
        throw IllegalArgumentException("WeatherBean lives is empty")
    }

    val liveData = weatherBean.lives[0]

    // 将 windpower 转换为 km/h,windpower是级数
    val windSpeedKms = when (liveData.windpower) {
        "≤3" -> "6-19"
        "4" -> "20-28"
        "5" -> "29-38"
        "6" -> "39-49"
        "7" -> "50-61"
        "8" -> "62-74"
        "9" -> "75-88"
        "10" -> "89-102"
        "11" -> "103-117"
        "12" -> "117-134"
        else -> 0
    }





    return WeatherData(
        weather = liveData.weather,
        location = liveData.city, // 使用 API 返回的城市名称
        temperature = "${liveData.temperature}°C",
        windSpeed = "${windSpeedKms} km/h", // 转换为 km/h
        humidity = "${liveData.humidity}%"
    )
}