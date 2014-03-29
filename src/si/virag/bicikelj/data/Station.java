package si.virag.bicikelj.data;

import android.location.Location;
import android.util.Log;

public class Station
{
	private final int id;
	private final String name;
	// Location
	private final String address;
	private final String fullAddress;
	private final Location location;
	
	// Is it open?
	private final boolean open;
	// Current status
	private int totalSpaces;
	private int freeSpaces;
	private int availableBikes;
	
	// Current distance
	private Float distance = null;

	public Station(int id,
                   String name,
                   String address,
                   String fullAddress,
			       double latitude,
                   double longtitude,
                   boolean open)
	{
		super();
		this.id = id;
		this.name = name;
		this.address = address;
		this.fullAddress = fullAddress;
		
		location = new Location("");
		location.setLatitude(latitude);
		location.setLongitude(longtitude);
		
		this.open = open;
	}
	
	public void setDistance(Location currentLocation)
	{
		if (location == null || currentLocation == null)
		{
			distance = null;
			return;
		}
		
		distance = location.distanceTo(currentLocation);
		Log.d(this.toString(), "Distance " + distance);
	}

	public Float getDistance()
	{
		return distance;
	}
	
	public Location getLocation()
	{
		return location;
	}
	
	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name.replaceAll("-", "\n");
	}

	public String getAddress()
	{
		return address;
	}

	public String getFullAddress()
	{
		return fullAddress;
	}

	public boolean isOpen()
	{
		return open;
	}

	public int getTotalSpaces()
	{
		return totalSpaces;
	}

	public int getFreeSpaces()
	{
		return freeSpaces;
	}

	public int getAvailableBikes()
	{
		return availableBikes;
	}

	public void setTotalSpaces(int totalSpaces)
	{
		this.totalSpaces = totalSpaces;
	}

	public void setFreeSpaces(int freeSpaces)
	{
		this.freeSpaces = freeSpaces;
	}

	public void setAvailableBikes(int availableBikes)
	{
		this.availableBikes = availableBikes;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}

	@Override
	public boolean equals(Object o)
	{
        if (!(o instanceof Station))
            return false;

		Station s = (Station)o;
		return (s.hashCode() == this.hashCode());
	}
	
	@Override
	public int hashCode()
	{
		return location.hashCode() + name.hashCode();
	}
}
