package si.virag.bicikelj.station_map

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.model.*
import de.greenrobot.event.EventBus
import si.virag.bicikelj.BicikeljApplication
import si.virag.bicikelj.MainActivity
import si.virag.bicikelj.R
import si.virag.bicikelj.data.Station
import si.virag.bicikelj.events.FocusOnStationEvent
import si.virag.bicikelj.location.LocationProvider
import si.virag.bicikelj.stations.StationRepository
import si.virag.bicikelj.util.DisplayUtils
import java.util.*
import javax.inject.Inject

class StationMapFragment : Fragment(), OnInfoWindowClickListener, Observer<Location?> {

    private lateinit var mapView: MapView

    private var map: GoogleMap? = null
    private var stations: List<Station> = listOf()
    private var markerMap: MutableMap<Marker, Station> = mutableMapOf()
    private var focusStationId: Int? = null

    private var rightInset = 0
    private var bottomInset = 0

    @Inject
    lateinit var locationProvider: LocationProvider
    @Inject
    lateinit var stationsRepository: StationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BicikeljApplication.component(context).inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        focusStationId = arguments?.getInt("focusOnStation")
        if (focusStationId == null) {
            focusStationId = savedInstanceState?.getInt("focusOnStationId")
        }

        markerMap = HashMap()
        locationProvider.observe(this, this)
        stationsRepository.getStations().observe(this, Observer {
            if (it.status == StationRepository.Status.SUCCESS) {
                it.stationInfo?.stations?.let { s ->
                    stations = s
                    setupMap()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.map_fragment, container, false)
        view.setOnApplyWindowInsetsListener { _: View, windowInsets: WindowInsets ->
            applyWindowInsetsListener(windowInsets)
        }
        mapView = view.findViewById(R.id.map_map)
        mapView.onCreate(savedInstanceState)
        return view
    }

    private fun applyWindowInsetsListener(windowInsets: WindowInsets): WindowInsets {
        rightInset = windowInsets.systemWindowInsetRight
        bottomInset = windowInsets.systemWindowInsetBottom
        map?.setPadding(0, 0, rightInset, bottomInset)
        return windowInsets
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(activity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            val activity: Activity? = activity
            activity?.overridePendingTransition(R.anim.slide_in_left,
                    R.anim.slide_out_right)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showDirections(selectedStation: Station) {
        val mapsUri = when (val location = locationProvider.value) {
            null -> "http://maps.google.com/maps?dirflg=w&daddr=${selectedStation.location.latitude},${selectedStation.location.longitude}"
            else -> "http://maps.google.com/maps?dirflg=w&saddr=${location.latitude},${location.longitude}&daddr=${selectedStation.location.latitude},${selectedStation.location.longitude}"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUri))
        // This prevents app selection pop-up
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("Bicikelj", "Google maps is not installed!")
        }
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        mapView.getMapAsync { googleMap: GoogleMap? ->
            map = googleMap
            setupMap()
        }
        EventBus.getDefault().registerSticky(this)
    }

    private fun setupMap() {
        if (map == null || context == null) {
            return
        }

        val map = map!!
        MapsInitializer.initialize(context)
        map.setPadding(0, 0, rightInset, bottomInset)
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_dark))
        }
        map.clear()
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        try {
            map.isMyLocationEnabled = true
        } catch (e: SecurityException) { // Nothing TBD
        }
        map.setInfoWindowAdapter(InformationAdapter())
        map.setOnInfoWindowClickListener(this)
        var update: CameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(MAP_CENTER_LAT,
                        MAP_CENTER_LNG),
                14.0f)
        if (focusStationId != null) {
            val focusedStation = stations.find { it.id == focusStationId }
            focusedStation?.location?.let {
                update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(focusedStation.location.latitude,
                                focusedStation.location.longitude), 16.0f)
            }
        } else {
            locationProvider.value?.let {
                update = CameraUpdateFactory.newLatLng(
                        LatLng(it.latitude, it.longitude))
            }
        }

        map.moveCamera(update)
        activity!!.invalidateOptionsMenu()
        Handler().postDelayed({ createMarkers() }, 300)
        mapView.postDelayed({ mapView.visibility = View.VISIBLE }, 150)
    }

    private fun createMarkers() {
        map?.let { map ->
            stations.filter { it.location != null }
                    .forEach { station ->
                        val marker = BitmapDescriptorFactory.defaultMarker(
                                station.availableBikes.toFloat() / station.totalSpaces.toFloat() * 120.0f)
                        val m = map.addMarker(MarkerOptions().position(
                                LatLng(station.location.latitude,
                                        station.location.longitude)).flat(true).icon(marker))
                        markerMap[m] = station
                        if (station.id == focusStationId) {
                            m.showInfoWindow()
                        }
                    }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        markerMap[marker]?.let {
            showDirections(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        focusStationId?.let {
            outState.putInt("focusOnStationId", it)
        }
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onChanged(t: Location?) {
        stations.forEach { it.setDistance(t) }
    }

    fun onEventMainThread(focusData: FocusOnStationEvent) {
        val entry = markerMap.entries.find { it.value.id == focusData.id }
        val targetMarker = entry?.key
        val targetStation = entry?.value
        if (targetMarker == null || targetStation == null) {
            return
        }

        targetMarker.showInfoWindow()
        val update = CameraUpdateFactory.newLatLngZoom(
                LatLng(targetStation.location.latitude,
                        targetStation.location.longitude), 16.0f)
        map?.animateCamera(update)
    }

    inner class InformationAdapter : InfoWindowAdapter {
        override fun getInfoWindow(marker: Marker): View {
            marker.setInfoWindowAnchor(0.5f, -0.2f)
            val view = LayoutInflater.from(context)
                    .inflate(R.layout.map_info, mapView, false)
            val tv = view.findViewById<TextView>(R.id.map_info_text)
            val s = markerMap[marker] ?: return tv
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.0f)
            val ssb = SpannableStringBuilder()
            ssb.append(s.name)
            ssb.setSpan(StyleSpan(Typeface.BOLD), 0, ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.append("\n")
            val startOfNumberHint = ssb.length
            // Bikes hint text
            ssb.append(context!!.getString(R.string.map_hint_bikes))
            ssb.append(" ")
            val bikesStr = s.availableBikes.toString()
            ssb.append(bikesStr)
            ssb.setSpan(ForegroundColorSpan(context!!.getColor(R.color.station_number_full)),
                    ssb.length - bikesStr.length, ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(StyleSpan(Typeface.BOLD), ssb.length - bikesStr.length,
                    ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Free hint text
            ssb.append(" ")
            ssb.append(context!!.getString(R.string.map_hint_free))
            ssb.append(" ")
            val freeStr = s.freeSpaces.toString()
            ssb.append(freeStr)
            ssb.setSpan(ForegroundColorSpan(context!!.getColor(R.color.station_number_free)),
                    ssb.length - freeStr.length, ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(StyleSpan(Typeface.BOLD), ssb.length - freeStr.length, ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(AbsoluteSizeSpan(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14.0f,
                            context!!.resources
                                    .displayMetrics).toInt()),
                    startOfNumberHint, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (s.distance != null) {
                ssb.append("\n")
                val distanceString = DisplayUtils.formatDistance(s.distance)
                ssb.append(distanceString)
                ssb.setSpan(AbsoluteSizeSpan(
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11.0f,
                                context!!.resources
                                        .displayMetrics).toInt()),
                        ssb.length - distanceString.length, ssb.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSpan(ForegroundColorSpan(Color.GRAY),
                        ssb.length - distanceString.length, ssb.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                        ssb.length - distanceString.length, ssb.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tv.text = ssb
            return view
        }

        override fun getInfoContents(marker: Marker): View? {
            return null
        }
    }

    companion object {
        private const val MAP_CENTER_LAT = 46.051367
        private const val MAP_CENTER_LNG = 14.506542
    }
}