package com.example.weatherwise

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherwise.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding : ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (isLocationPermissionGranted()) {
            // Permission already granted, proceed to get the current location
            getCurrentLocation()
        } else {
            // Permission not granted, request it with options
            requestLocationPermission()
        }
        SearchCity()
    }
    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // Now you have the current location
                        Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
                        currentLocWeather(latitude,longitude)

                    } else {
                        showLocationErrorDialog("Location not available")
                    }
                }
        } catch (e: SecurityException) {
            showLocationErrorDialog("Location permission not granted")
        }
    }
    private fun showLocationErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Location Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }


    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }



    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, get current location
                    getCurrentLocation()
                } else {
                    showLocationErrorDialog("Location Permission is needed!")
                    // Permission denied, handle accordingly
                    // You might want to show a message to the user or ask again later
                }
            }
        }
    }
    private fun currentLocWeather(latitude : Double , longitude : Double){
        val retrofit= Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(currentInterface::class.java)
        val response=retrofit.getCurrentLocationWeather(latitude,longitude,"3f2835be534ca6ab89347887d964b726")
        response.enqueue(object: Callback<currentweather> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<currentweather>, response: Response<currentweather>) {
                val responseBody = response.body()
                if(response.isSuccessful && responseBody!=null){
                    //Log.d("city","$location")
                    //Log.d("temp","$temperature")
                    //val dateTimeFormatter=DateTimeFormatter.ofPattern("hh:mm a")
                    val humidity=responseBody.main.humidity.toString()
                    val sunset = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(responseBody.sys.sunset.toLong() * 1000))
                    val sunrise= SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(responseBody.sys.sunrise.toLong() * 1000))
                    val wind=responseBody.wind.speed.toString()
                    val condition=responseBody.weather.firstOrNull()?.main?:"unknown"
                    val sealevel=responseBody.main.sea_level.toString()
                    binding.city.text=" "+responseBody.name.capitalize()
                    binding.temperature.text = String.format(Locale.getDefault(), "%.2f °C", responseBody.main.temp - 273.15)
                    binding.weather.text=condition
                    binding.minTemp.text=String.format(Locale.getDefault(),"Min Temp: %.2f °C",responseBody.main.temp_min-273.15)
                    binding.maxTemp.text=String.format(Locale.getDefault(),"Max Temp%.2f °C",responseBody.main.temp_max-273.15)
                    binding.humidity.text="$humidity %"
                    binding.windspeed.text="$wind m/s"
                    binding.condition.text="$condition"
                    binding.sealevel.text="$sealevel hPa"
                    binding.sunrise.text="$sunrise"
                    binding.sunset.text="$sunset"
                    binding.Day.text=dayName(System.currentTimeMillis())
                    binding.date.text=date()
                    changeBackground(condition,responseBody.sys.sunrise.toLong(),responseBody.sys.sunset.toLong())

                }
            }

            override fun onFailure(call: Call<currentweather>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
    fun dayName(timestamp:Long):String{
        val sdf= SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun date():String{
        val sdf= SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun SearchCity() {
        val searchView=binding.searchbar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                fetchWeatherData(p0?.capitalize())
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(city: String?) {
        if(city.isNullOrBlank()){
            Toast.makeText(this,"Enter the city", Toast.LENGTH_SHORT).show()
        }

        val retrofit= Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(apiInterface::class.java)
        val response=retrofit.getWeatherData(city,"3f2835be534ca6ab89347887d964b726","metrics")
        response.enqueue(object: Callback<WeatherApp> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if(response.isSuccessful && responseBody!=null){
                    //Log.d("city","$location")
                    //Log.d("temp","$temperature")
                    //val dateTimeFormatter=DateTimeFormatter.ofPattern("hh:mm a")
                    val humidity=responseBody.main.humidity.toString()
                    val sunset = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(responseBody.sys.sunset.toLong() * 1000))
                    val sunrise= SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(responseBody.sys.sunrise.toLong() * 1000))
                    val wind=responseBody.wind.speed.toString()
                    val condition=responseBody.weather.firstOrNull()?.main?:"unknown"
                    val sealevel=responseBody.main.sea_level.toString()
                    binding.city.text= " $city"
                    binding.temperature.text = String.format(Locale.getDefault(), "%.2f °C", responseBody.main.temp - 273.15)
                    binding.weather.text=condition
                    binding.minTemp.text=String.format(Locale.getDefault(),"Min Temp: %.2f °C",responseBody.main.temp_min-273.15)
                    binding.maxTemp.text=String.format(Locale.getDefault(),"Max Temp%.2f °C",responseBody.main.temp_max-273.15)
                    binding.humidity.text="$humidity %"
                    binding.windspeed.text="$wind m/s"
                    binding.condition.text="$condition"
                    binding.sealevel.text="$sealevel hPa"
                    binding.sunrise.text="$sunrise"
                    binding.sunset.text="$sunset"
                    binding.Day.text=dayName(System.currentTimeMillis())
                    binding.date.text=date()

                    changeBackground(condition,responseBody.sys.sunrise.toLong(),responseBody.sys.sunset.toLong())
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun changeBackground(condition: String, sunrise: Long, sunset: Long) {
        val currentTimeMillis = System.currentTimeMillis()
        val isDayTime = currentTimeMillis in sunrise * 1000..sunset * 1000

        when {
            isDayTime -> {
                when (condition) {
                    "Haze", "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy", "Fog" -> {
                        binding.root.setBackgroundResource(R.drawable.colud_background)
                        binding.lottieAnimationView.setAnimation(R.raw.cloud)
                    }
                    "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rainy" -> {
                        binding.root.setBackgroundResource(R.drawable.rain_background)
                        binding.lottieAnimationView.setAnimation(R.raw.rain)
                    }
                    "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                        binding.root.setBackgroundResource(R.drawable.snow_background)
                        binding.lottieAnimationView.setAnimation(R.raw.snow)
                    }
                    "Clear Sky", "Sunny", "Clear", "Hot" -> {
                        // Handle clear sky background for day
                        binding.root.setBackgroundResource(R.drawable.sunny_background)
                        binding.lottieAnimationView.setAnimation(R.raw.sun)
                    }
                }
            }
            else -> {
                // Night time
                when (condition) {

                    "Clear Sky", "Sunny", "Clear", "Hot" -> {
                        // Handle clear sky background for night
                        binding.temperature.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.textView4.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.minTemp.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.maxTemp.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.date.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.Day.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.city.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.weather.setTextColor(Color.parseColor("#FFFFFF"))
                        binding.root.setBackgroundResource(R.drawable.nightclearsky)
                        binding.lottieAnimationView.setAnimation(R.raw.moon)
                    }
                }
            }
        }
    }
}