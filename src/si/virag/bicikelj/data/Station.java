package si.virag.bicikelj.data;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Station implements Parcelable
{
	private int id;
	private String name;
	// Location
	private String address;
	private String fullAddress;
	private Location location;
	
	// Is it open?
	private boolean open;
	// Current status
	private int totalSpaces;
	private int freeSpaces;
	private int availableBikes;
	
	// Current distance
	private Float distance = null;

	public Station(int id, String name, String address, String fullAddress,
			double latitude, double longtitude, boolean open)
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
		Log.d("Station", "Returning " + this.name);
		return this.name;
	}

	@Override
	public boolean equals(Object o)
	{
		Station s = (Station)o;
		return (s.hashCode() == this.hashCode());
	}
	
	@Override
	public int hashCode()
	{
		return (int) (location.hashCode() + name.hashCode());
	}

	
	/**
	 * 
	 * Implementation of Parcelable methods
	 */
	
	public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() 
	{
		@Override
		public Station createFromParcel(Parcel source) {
			int id = source.readInt();
			String name = source.readString();
			String address = source.readString();
			String fullAddress = source.readString();
			double lat = source.readDouble();
			double lng = source.readDouble();
			boolean open = source.readByte() == 1;
			int free = source.readInt();
			int bikes = source.readInt();
			int total = source.readInt();
			
			Station station = new Station(id, name, address, fullAddress, lat, lng, open);
			station.setFreeSpaces(free);
			station.setAvailableBikes(bikes);
			station.setTotalSpaces(total);
			return station;
		}

		@Override
		public Station[] newArray(int size) {
			return new Station[size];
		}
	};
	
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(address);
		dest.writeString(fullAddress);
		dest.writeDouble(location.getLatitude());
		dest.writeDouble(location.getLongitude());
		dest.writeByte((byte) (open ? 1 : 0));
		dest.writeInt(freeSpaces);
		dest.writeInt(availableBikes);
		dest.writeInt(totalSpaces);
	}
}
