package si.virag.bicikel.map;

public class StationMarker
{
	private double latitude;
	private double longtitude;
	private String description;
	
	public StationMarker(double latitude, double longtitude, String description)
	{
		super();
		this.latitude = latitude;
		this.longtitude = longtitude;
		this.description = description;
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
