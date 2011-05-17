package si.virag.bicikel.data;

import java.util.ArrayList;

public class StationInfo
{
	private ArrayList<Station> stations;
	
	public StationInfo()
	{
		stations = new ArrayList<Station>();
	}
	
	public void addStation(Station station)
	{
		stations.add(station);
	}
	
	public ArrayList<Station> getStations()
	{
		return stations;
	}
}
