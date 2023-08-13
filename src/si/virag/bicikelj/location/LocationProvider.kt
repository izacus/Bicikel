package si.virag.bicikelj.location

import android.content.Context
import android.location.Location
import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(context: Context) : MutableLiveData<Location?>() {

    private val TAG = "LocationProvider"

    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
            context)
    private val locationThread = HandlerThread("LocationUpdates")
    private val callback = Callback()

    override fun onInactive() {
        Log.d(TAG, "Deactivating updates.")
        client.removeLocationUpdates(callback)
    }

    override fun onActive() {
        Log.d(TAG, "Activating updates.")
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60000).build()
        try {
            client.lastLocation.addOnCompleteListener { postValue(it.result) }
            client.requestLocationUpdates(request, callback, locationThread.looper)
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to activate location.", e)
        }
    }

    /** Called when permissions are requested. */
    fun reactivate() {
        onActive()
    }

    inner class Callback : LocationCallback() {
        override fun onLocationResult(loc: LocationResult) {
            Log.d(TAG, "Location: $loc")
            postValue(loc?.lastLocation)
        }
    }
}