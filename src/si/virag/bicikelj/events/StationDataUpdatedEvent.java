package si.virag.bicikelj.events;

import java.util.List;

import si.virag.bicikelj.data.Station;

public class StationDataUpdatedEvent
{
    private final List<Station> stations;

    public StationDataUpdatedEvent(List<Station> stations)
    {
        this.stations = stations;
    }

    public List<Station> getStations()
    {
        return stations;
    }
}
