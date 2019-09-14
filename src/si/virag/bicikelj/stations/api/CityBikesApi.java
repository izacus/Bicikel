package si.virag.bicikelj.stations.api;

import retrofit2.Call;
import retrofit2.http.GET;
import si.virag.bicikelj.data.StationInfo;

public interface CityBikesApi {

    @GET("/v2/networks/bicikelj")
    Call<StationInfo> getStationData();

}
