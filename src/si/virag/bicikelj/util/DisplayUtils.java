package si.virag.bicikelj.util;

public class DisplayUtils 
{

	public static String formatDistance(Float distance)
	{
		// Using german locale, because slovene locale is not available on all devices
		// and germany uses same number format
		if (distance < 1200)
		{
			return String.format("%,.1f", distance) + " m";
		}
		else
		{
			return String.format("%,.2f", distance / 1000) + " km";
		}
	}	
}
