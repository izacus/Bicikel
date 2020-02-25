package si.virag.bicikelj.location

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(context: Context) : LifecycleObserver {

    private val TAG = "LocationProvider"

    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            context)
    private val locationLiveData: MutableLiveData<Location> = MutableLiveData();
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            if (result != null && result.lastLocation != null) {
                locationLiveData.postValue(result.lastLocation)
            }
        }
    }

    fun locationLiveData(): LiveData<Location> {
        try {
            client.lastLocation.addOnSuccessListener {
                if (it != null) {
                    locationLiveData.postValue(it)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No access to location for location update.", e)
        }

        requestNewUpdate()
        return locationLiveData
    }

    fun requestNewUpdate() {
        try {
            client.requestLocationUpdates(LocationRequest.create().setNumUpdates(1).setPriority(
                    LocationRequest.PRIORITY_LOW_POWER).setMaxWaitTime(
                    10000), locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            Log.e(TAG, "No access to location for location update.", e)
        }
    }
}