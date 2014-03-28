package si.virag.bicikelj.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.location.Location;

public class StationInfo
{
	private long timeUpdated;
	private ArrayList<Station> stations;
	
	private boolean disancesValid = false;
	
	public long getTimeUpdated()
	{
		return timeUpdated;
	}

	public StationInfo(long timeUpdated)
	{
		stations = new ArrayList<Station>();
		this.timeUpdated = timeUpdated;
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
		if (disancesValid)
			return;
		
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
		
		disancesValid = true;
	}

	public StationInfo getFilteredInfo(String filterText)
	{
		StationInfo newInfo = new StationInfo(timeUpdated);
		
		for (Station station : stations)
		{
			if (filterSanitize(station.getName()).contains(filterSanitize(filterText))) {
				newInfo.addStation(station);
			}
		}
		
		return newInfo;
	}
	
	private static String filterSanitize(String text)
	{
		return text.trim().toUpperCase().replace('Č', 'C').replace('Š','S').replace('Ž', 'Z');
	}
}
