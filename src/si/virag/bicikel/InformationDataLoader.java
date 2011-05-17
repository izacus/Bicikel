package si.virag.bicikel;

import java.net.MalformedURLException;
import java.net.URL;

import si.virag.bicikel.data.StationInfo;
import si.virag.bicikel.util.AsyncLoader;
import android.content.Context;
import android.util.Log;

public class InformationDataLoader extends AsyncLoader<StationInfo>
{

	public InformationDataLoader(Context context)
	{
		super(context);
	}

	@Override
	public StationInfo loadInBackground()
	{
		Log.i(this.toString(), "Loading station data from server...");
		
		try
		{
			URL url = new URL("http://www.bicikelj.si/service/carto");
		}
		catch (MalformedURLException e)
		{}
	
		return null;
	}

	
	
}
