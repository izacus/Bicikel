package si.virag.bicikel.data;

public class Station
{
	private int id;
	private String name;
	// Location
	private String address;
	private String fullAddress;
	private double latitude;
	private double longtitude;
	// Is it open?
	private boolean open;
	// Current status
	private int totalSpaces;
	private int freeSpaces;
	private int availableBikes;

	public Station(int id, String name, String address, String fullAddress,
			double latitude, double longtitude, boolean open)
	{
		super();
		this.id = id;
		this.name = name;
		this.address = address;
		this.fullAddress = fullAddress;
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.open = open;
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getAddress()
	{
		return address;
	}

	public String getFullAddress()
	{
		return fullAddress;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongtitude()
	{
		return longtitude;
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
	public boolean equals(Object o)
	{
		Station s = (Station)o;
		return (s.hashCode() == this.hashCode());
	}
	
	@Override
	public int hashCode()
	{
		return (int) (latitude + longtitude + name.hashCode());
	}
	
	
}
