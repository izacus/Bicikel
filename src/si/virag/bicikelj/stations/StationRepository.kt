package si.virag.bicikelj.stations

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import si.virag.bicikelj.data.StationInfo
import si.virag.bicikelj.stations.api.CityBikesApiClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationRepository @Inject constructor() : Callback<StationInfo> {

    enum class Status {
        SUCCESS,
        FAILURE
    }

    data class Stations(val stationInfo: StationInfo?, val status: Status)

    private var stations: MutableLiveData<Stations> = MutableLiveData()
    private var callInProgress: Call<StationInfo>? = null

    fun getStations(): LiveData<Stations> {
        if (stations.value == null) {
            refresh()
        }

        return stations
    }

    fun refresh() {
        if (callInProgress != null) {
            return
        }

        val callInProgress = CityBikesApiClient.getBicikeljApi().stationData
        this.callInProgress = callInProgress
        callInProgress.enqueue(this)
    }

    override fun onResponse(call: Call<StationInfo>, response: Response<StationInfo>) {
        if (response.isSuccessful) {
            stations.postValue(Stations(response.body(), Status.SUCCESS))
        } else {
            stations.postValue(Stations(stations.value?.stationInfo, Status.FAILURE))
        }
    }

    override fun onFailure(call: Call<StationInfo>, t: Throwable) {
        Log.e("StationRepository", "Load failed", t)
        stations.postValue(Stations(stations.value?.stationInfo, Status.FAILURE))
    }
}