package com.madhusiri.app.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.madhusiri.app.R
import com.madhusiri.app.databinding.FragmentMapBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    private var selectedCircle: Circle? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var userRole: String? = null

    // Real-time animation components
    private var pulseAnimator: ValueAnimator? = null
    private var isLiveTrackingEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        userRole = requireContext().getSharedPreferences("MadhuSiriPrefs", Context.MODE_PRIVATE)
            .getString("USER_ROLE", "BEEKEEPER")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup unified banner context dynamic labels
        if (userRole == "FARMER") {
            binding.tvMapRoleTitle.text = "2km Emergency Scan"
            binding.tvMapHint.text = "Live active location scanning enabled"
        } else {
            binding.tvMapRoleTitle.text = "Hive Protection Fence"
        }

        // Connect user UI interaction controller toggle switch logic
        binding.switchLiveLocation.setOnCheckedChangeListener { _, isChecked ->
            isLiveTrackingEnabled = isChecked
            if (isChecked && userRole == "FARMER") {
                startLocationUpdatesForFarmer()
                Snackbar.make(binding.root, "Live tracking re-engaged", Snackbar.LENGTH_SHORT).show()
            } else if (!isChecked && userRole == "FARMER") {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                Snackbar.make(binding.root, "Live tracking paused", Snackbar.LENGTH_SHORT).show()
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Apply Premium Custom JSON Map Style configured specifically for OLED dark view integration
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark)
            )
            if (!success) {
                Log.e("MapFragment", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapFragment", "Can't find dark style JSON resource.", e)
        }

        // Configure custom padding ensuring Map UI controls are never hidden behind our floating UI banner sheet
        mMap.setPadding(0, 360, 0, 240)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false

        binding.fabMyLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    } ?: run {
                        Snackbar.make(binding.root, "Searching for GPS signal...", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (userRole == "FARMER") {
            startLocationUpdatesForFarmer()
            binding.fabSaveLocation.visibility = View.GONE
        } else { // BEEKEEPER flow logic
            val sharedPrefs = requireContext().getSharedPreferences("MadhuSiriPrefs", Context.MODE_PRIVATE)
            val savedLat = sharedPrefs.getFloat("HIVE_LAT", 0f)
            val savedLng = sharedPrefs.getFloat("HIVE_LNG", 0f)

            if (savedLat != 0f) {
                val savedLoc = LatLng(savedLat.toDouble(), savedLng.toDouble())
                updateMarkerAndCircle(savedLoc, getString(R.string.map_your_hive_location), true)
                // Optimized Initial Zoom Level targeted precisely at city bounds visualization scale (14f vs old 10f)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(savedLoc, 14f))
            } else {
                val defaultLoc = LatLng(12.9716, 77.5946)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 12f))
            }

            mMap.setOnMapLongClickListener { latLng ->
                updateMarkerAndCircle(latLng, getString(R.string.map_new_hive_location), false)
                binding.fabSaveLocation.visibility = View.VISIBLE
                binding.fabSaveLocation.scaleX = 0f
                binding.fabSaveLocation.scaleY = 0f
                binding.fabSaveLocation.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
            }

            binding.fabSaveLocation.setOnClickListener {
                selectedMarker?.let { marker ->
                    with(sharedPrefs.edit()) {
                        putFloat("HIVE_LAT", marker.position.latitude.toFloat())
                        putFloat("HIVE_LNG", marker.position.longitude.toFloat())
                        apply()
                    }
                    binding.fabSaveLocation.animate().scaleX(0f).scaleY(0f).setDuration(150)
                        .withEndAction { binding.fabSaveLocation.visibility = View.GONE }.start()
                    
                    Snackbar.make(binding.root, getString(R.string.map_hive_location_saved), Snackbar.LENGTH_SHORT).show()
                    // Transform temporary marker into confirmed status state
                    updateMarkerAndCircle(marker.position, getString(R.string.map_your_hive_location), true)
                }
            }
        }
    }

    private fun startLocationUpdatesForFarmer() {
        if (!isLiveTrackingEnabled) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L
        ).setMinUpdateIntervalMillis(3000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    updateMarkerAndCircle(currentLocation, "Current Spray Origin", true)
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation))
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun updateMarkerAndCircle(latLng: LatLng, title: String, isConfirmed: Boolean) {
        selectedMarker?.remove()
        selectedCircle?.remove()
        pulseAnimator?.cancel()

        // Construct customized vector graphic map drop elements
        val markerColorRes = if (userRole == "FARMER") R.color.danger_red else R.color.honey_gold
        val descriptor = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_pin_custom, markerColorRes)

        selectedMarker = mMap.addMarker(
            MarkerOptions().position(latLng).title(title).icon(descriptor)
        )

        // Setup real-time pulsing danger zone geofence styling (red primary base replacing opaque gold)
        val strokeColor = if (userRole == "FARMER") Color.parseColor("#FF5252") else Color.parseColor("#FFB800")
        val baseFillColor = if (userRole == "FARMER") Color.parseColor("#26FF5252") else Color.parseColor("#1AFFB800")

        selectedCircle = mMap.addCircle(
            CircleOptions()
                .center(latLng)
                .radius(2000.0) // Precise 2km danger fence
                .strokeWidth(5f)
                .strokeColor(strokeColor)
                .fillColor(baseFillColor)
        )

        // Integrate high-fidelity continuous pulse animation simulating sensor scan activity
        if (isConfirmed) {
            pulseAnimator = ValueAnimator.ofInt(15, 50).apply {
                duration = 1500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                addUpdateListener { animator ->
                    val alpha = animator.animatedValue as Int
                    val colorHex = if (userRole == "FARMER") "#%02XFF5252".format(alpha) else "#%02XFFB800".format(alpha)
                    selectedCircle?.fillColor = Color.parseColor(colorHex)
                }
                start()
            }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, tintResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setTint(ContextCompat.getColor(context, tintResId))
            setBounds(0, 0, intrinsicWidth.coerceAtLeast(80), intrinsicHeight.coerceAtLeast(80))
            val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onPause() {
        super.onPause()
        if (userRole == "FARMER") {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        pulseAnimator?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (userRole == "FARMER" && isLiveTrackingEnabled) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startLocationUpdatesForFarmer()
            }
        }
        pulseAnimator?.resume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pulseAnimator?.cancel()
        _binding = null
    }
}
