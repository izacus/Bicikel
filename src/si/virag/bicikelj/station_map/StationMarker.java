package si.virag.bicikelj.station_map;

public class StationMarker
{
	private double latitude;
	private double longtitude;
	private String description;
	private int free;
	private int bikes;
	
	public StationMarker(double latitude, double longtitude, String description, int free, int bikes)
	{
		super();
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.description = description;
		this.free = free;
		this.bikes = bikes;
	}

	public int getFree()
	{
		return free;
	}

	public int getBikes()
	{
		return bikes;
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongtitude()
	{
		return longtitude;
	}

	public String getDescription()
	{
		return description;
	}
}
