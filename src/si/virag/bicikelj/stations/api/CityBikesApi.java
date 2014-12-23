package si.virag.bicikelj.stations.api;

import retrofit.Callback;
import retrofit.http.GET;
import si.virag.bicikelj.data.StationInfo;

public interface CityBikesApi {

    @GET("/v2/networks/bicikelj")
    void getStationData(Callback<StationInfo> stationInfo);

}
