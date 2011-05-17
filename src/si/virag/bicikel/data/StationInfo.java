package si.virag.bicikel.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.location.Location;

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
	
	public void calculateDistances(Location currentLocation)
	{
		for (Station station : stations)
		{
			station.setDistance(currentLocation);
		}
		
		Collections.sort(stations, new Comparator<Station>()
		{
			@Override
			public int compare(Station object1, Station object2)
			{
				return object1.getDistance().compareTo(object2.getDistance());
			}
			
		});
	}
}
