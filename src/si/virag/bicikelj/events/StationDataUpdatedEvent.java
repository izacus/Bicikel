package si.virag.bicikelj.events;

import si.virag.bicikelj.data.Station;

import java.util.ArrayList;

/**
 * Created by jernej on 29/03/14.
 */
public class StationDataUpdatedEvent
{
    private final ArrayList<Station> stations;

    public StationDataUpdatedEvent(ArrayList<Station> stations)
    {
        this.stations = stations;
    }

    public ArrayList<Station> getStations()
    {
        return stations;
    }
}
