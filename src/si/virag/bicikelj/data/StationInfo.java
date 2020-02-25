package si.virag.bicikelj.data;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StationInfo {
    @NonNull
    @SerializedName("network")
    private CityBikesNetwork network;

    public Calendar getTimeUpdated() {
        if (network.stations.size() == 0) {
            return null;
        }

        return network.stations.get(0).getUpdated();
    }

    private StationInfo() {
        network = new CityBikesNetwork();
        network.stations = new ArrayList<>();
    }

    private void addStation(Station station) {
        network.stations.add(station);
    }

    public List<Station> getStations() {
        return network.stations;
    }

    public StationInfo getFilteredInfo(String filterText) {
        StationInfo newInfo = new StationInfo();

        for (Station station : getStations()) {
            if (filterSanitize(station.getName()).contains(filterSanitize(filterText))) {
                newInfo.addStation(station);
            }
        }

        return newInfo;
    }

    private static String filterSanitize(String text) {
        return text.trim().toUpperCase().replace('Č', 'C').replace('Š', 'S').replace('Ž', 'Z');
    }

    private static class CityBikesNetwork {
        @SerializedName("stations")
        @NonNull
        public List<Station> stations;
    }
}
