package si.virag.bicikelj.station_map;

import android.location.Location;

public class StationMarker
{
	private int id;
	private Location location;	
	
	public StationMarker(int id, Location location)
	{
		super();
		this.location = location;
		this.id = id;
	}

	public int getId()
	{
		return id;
	}
	
	public Location getLocation()
	{
		return location;
	}
}
