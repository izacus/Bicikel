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


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.fullAddress);
        dest.writeParcelable(this.location, 0);
        dest.writeByte(open ? (byte) 1 : (byte) 0);
        dest.writeInt(this.totalSpaces);
        dest.writeInt(this.freeSpaces);
        dest.writeInt(this.availableBikes);
        dest.writeValue(this.distance);
    }

    private Station(Parcel in)
    {
        this.id = in.readInt();
        this.name = in.readString();
        this.address = in.readString();
        this.fullAddress = in.readString();
        this.location = in.readParcelable(Location.class.getClassLoader());
        this.open = in.readByte() != 0;
        this.totalSpaces = in.readInt();
        this.freeSpaces = in.readInt();
        this.availableBikes = in.readInt();
        this.distance = (Float) in.readValue(Float.class.getClassLoader());
    }

    public static Creator<Station> CREATOR = new Creator<Station>()
    {
        public Station createFromParcel(Parcel source)
        {
            return new Station(source);
        }

        public Station[] newArray(int size)
        {
            return new Station[size];
        }
    };
}
