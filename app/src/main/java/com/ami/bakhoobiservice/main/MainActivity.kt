package com.ami.bakhoobiservice.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ami.bakhoobiservice.adapters.CurrentlyRunningAppAdapter
import com.ami.bakhoobiservice.databinding.ActivityMainBinding
import com.ami.bakhoobiservice.most_recent_applications.UStats
import com.ami.bakhoobiservice.roomDB.AppInfoDatabase
import com.ami.bakhoobiservice.roomDB.AppInformationEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var appInfoDatabase: AppInfoDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val LOG = "MainActvity_Ami"

    val sharedPreference = getSharedPreferences("Last_Location", Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        setContentView(binding.root)


        //requestPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        binding.usedApp.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (UStats.getUsageStatsList(this).isEmpty()) {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivity(intent)
                }
            }
        }

        binding.lastLocation.setOnClickListener {
            putInLastLocationLocation("Unknown", false)
        }

        binding.infoLocation.setOnClickListener {
            lastLocation()
        }


        binding.currentlyRunningApps.setOnClickListener {
            currentlyRunningApps()
        }

    }


    fun lastLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.listOfRunningTasks.visibility = View.VISIBLE


        //Considering the permission has been given already

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(listener: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener {
                if (it == null)
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {

                    val lat = it.latitude
                    val lon = it.longitude
                    binding.cvCurrentLocationDetails.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.tvLatitude.text = "Latitude: $lat"
                    binding.tvLongitude.text = "Longitude: $lon"
                    saveLocationInRoomDb("Latitude: $lat and Longitude: $lon")
                }
            }
    }


    private fun statusCheck() {


    }


    private fun showCommonDialog(
        title: String,
        msg: String,
        positiveButton: String,
        negativeButton: String
    ) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this);
        alertDialog.apply {
            setTitle(title)
            setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(positiveButton) { _: DialogInterface?, _: Int ->
                    startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton(negativeButton) { _, _ ->

                }
        }.create().show()

    }


    private fun saveLocationInRoomDb(location: String) {
        appInfoDatabase = AppInfoDatabase.getDB(this)
        GlobalScope.launch {
            appInfoDatabase.getContactDao().insertAppInfo(
                AppInformationEntity(
                    0, location
                )
            )
            putInLastLocationLocation(location, true)
        }

    }


    fun putInLastLocationLocation(location: String, shouldSave: Boolean) {
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        if (shouldSave) {
            editor.putString("location", location)
        } else {
            sharedPreference.getString("location", "Unknown")
            binding.lastLocationData.text = location
        }
        editor.commit()
    }

    fun getFromLastLocation() {

    }


    private fun currentlyRunningApps() {

        val listApp = mutableListOf<String>()
        binding.listOfRunningTasks.visibility = View.VISIBLE
        binding.cvCurrentLocationDetails.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)

        for (i in tasks.indices) {
            listApp.add(tasks[i].baseActivity!!.toShortString() + "\t\t ID: " + tasks[i].id)

            binding.listOfRunningTasks.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            val runningAppAdapter = CurrentlyRunningAppAdapter(this, listApp)
            binding.listOfRunningTasks.adapter = runningAppAdapter


        }
    }

}


