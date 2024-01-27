package com.example.weather_app

import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weather_app.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.shashank.sony.fancytoastlib.FancyToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.BindException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//3f2835be534ca6ab89347887d964b726
class MainActivity : AppCompatActivity() {
    private val binding:ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (isLocationPermissionGranted()) {
            getCurrentLocation();
            // Permission already granted, proceed to get the current location
        } else {
            // Permission not granted, request it
            requestLocationPermission()
        }

        SearchCity()
    }
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        // Use reverse geocoding to get the city name
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                        if (addresses?.isNotEmpty()==true) {
                            val city = addresses[0].locality
                            fetchWeatherData(city)
                        } else {
                            // City not found in reverse geocoding
                            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        // Location is null, handle accordingly
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Error getting location: $e")
                    fetchWeatherData(null) // Use default location or handle accordingly
                }
        } catch (e: SecurityException) {
            Log.e("Location", "Security exception: $e")
            fetchWeatherData(null) // Use default location or handle accordingly
        }
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
                    Toast.makeText(this,"Location Permission is needed!",Toast.LENGTH_SHORT).show()
                    // Permission denied, handle accordingly
                    // You might want to show a message to the user or ask again later
                }
            }
        }
    }



    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, proceed to get the location
            getCurrentLocation()
        }
    }
    private fun SearchCity() {
        val searchView = binding.searchbar
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val capitalizedQuery = it.capitalize() // Capitalize the first letter
                    if (isCityValid(capitalizedQuery)) {
                        // City is valid, call fetchWeatherData
                        fetchWeatherData(capitalizedQuery)
                    } else {
                        if (capitalizedQuery.isNullOrBlank()) {
                            // Query is blank, show a toast message
                            showToast("Enter the city")
                        }
                    }
                }
                hideKeyboard(searchView)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }

    private fun isCityValid(city: String?): Boolean {
        // You can implement your own logic to determine if the city is valid
        // For example, you might check the length or perform a more sophisticated check
        return !city.isNullOrBlank()
    }

    private fun showToast(message: String) {
        FancyToast.makeText(this, message, FancyToast.LENGTH_SHORT, FancyToast.WARNING, true).show()
    }


    private fun fetchWeatherData(city: String? = null) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(apiInterface::class.java)

        // Use city name if provided, otherwise use coordinates
        val response = retrofit.getWeatherData(city, "3f2835be534ca6ab89347887d964b726", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        // Handle successful response
                        val temperature = responseBody.main.temp.toString()
                        val humidity = responseBody.main.humidity.toString()
                        val windspeed = responseBody.wind.speed
                        val sunrise = responseBody.sys.sunrise.toLong()
                        val sunset = responseBody.sys.sunset.toLong()
                        val sealevel = responseBody.main.pressure
                        val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                        val maxtemp = responseBody.main.temp_max.toString()
                        val mintemp = responseBody.main.temp_min.toString()

                        binding.temperature.text = "$temperature °C"
                        binding.weather.text = condition
                        binding.maxTemp.text = "Max Temp: $maxtemp°C"
                        binding.minTemp.text = "Min Temp: $mintemp°C"
                        binding.humidity.text = "$humidity%"
                        binding.condition.text = condition
                        binding.sunrise.text = "${formatTime(sunrise)}"
                        binding.sunset.text = "${formatTime(sunset)}"

                        // Check if name and country are not null before updating the city
                        val cityName = responseBody.name
                        val country = responseBody.sys.country
                        if (cityName != null && country != null) {
                            binding.city.text = "$cityName, $country"
                        }

                        binding.windspeed.text = "$windspeed m/s"
                        binding.sealevel.text = "$sealevel hPa"
                        binding.Day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                        // Change image according to weather condition
                        changeImageaccordingtoWeatherCondition(condition)
                    } else {
                        // Response body is null, handle accordingly
                        showToast("Cannot find the city")
                    }
                } else {
                    // Response is not successful (e.g., city not found), handle accordingly
                    showToast("Cannot find the city")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                // Handle failure
            }
        })
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }





    private fun changeImageaccordingtoWeatherCondition(condition:String) {
        when (condition){
            "Clear Sky","Sunny","Clear" ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Partly Clouds","Clouds","Overcast","Mist","Foggy","Haze","Fog" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain","Drizzle","Moderate Rain","Showers","Heavy Rain" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow","Moderate Snow","Heavy Snow","Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): CharSequence? {
        val sdf=SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun time(timestamp: Long): String {
        val sdf=SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp*1000))
    }
    fun dayName(timstamp:Long):String{
        val sdf=SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }



}
