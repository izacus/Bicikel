package si.virag.bicikelj.stations;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.util.HTTPHelper;

import java.io.IOException;
import java.util.Iterator;

public class JSONInformationDataLoader extends AsyncTaskLoader<StationInfo>
{
	private static final String STATION_LIST_URL = "http://prevoz.org/api/bicikelj/list/";
	private static final int CACHE_VALIDITY = 60 * 1000;	// ms
	
	private StationInfo cachedResults = null;
	private long lastUpdate = 0;
	
	public JSONInformationDataLoader(Context context)
	{
		super(context);
	}

	@Override
	public StationInfo loadInBackground()
	{
		String response;
		
		try
		{
			response = HTTPHelper.httpGet(STATION_LIST_URL);
		}
		catch (IOException e)
		{
			Log.e(this.toString(), "Error receiving data.", e);
			return null;
		}
		
		
		try
		{
			JSONObject object = new JSONObject(response);
			
			// Get time updated
			long timeUpdated = object.getLong("updated");
			
			StationInfo info = new StationInfo(timeUpdated);
			
			// Get marker array
			JSONObject markers = object.getJSONObject("markers");

			@SuppressWarnings("unchecked")
			Iterator<String> markerKeys = markers.keys();
			
			while(markerKeys.hasNext())
			{
				JSONObject stationObject = markers.getJSONObject(markerKeys.next());
				
				Station station = new Station(stationObject.getInt("number"), 
											  stationObject.getString("name"),
											  stationObject.getString("address"),
											  stationObject.getString("fullAddress"),
											  stationObject.getDouble("lat"),
											  stationObject.getDouble("lng"),
											  stationObject.getInt("open") == 1);
				
				JSONObject stationStatus = stationObject.getJSONObject("station");
				station.setAvailableBikes(stationStatus.getInt("available"));
				station.setFreeSpaces(stationStatus.getInt("free"));
				station.setTotalSpaces(stationStatus.getInt("total"));
				
				if (station.getTotalSpaces() > 0)
				{
					info.addStation(station);
				}

			}
			
			this.cachedResults = info;
			this.lastUpdate = System.currentTimeMillis();
			return info;
		}
		catch (JSONException e)
		{
			Log.e(this.toString(), "Error parsing response JSON.", e);
			return null;
		}
	}

	@Override
	protected void onReset() 
	{
		super.onReset();
		onStopLoading();
		lastUpdate = 0;
		cachedResults = null;
	}

	@Override
	protected void onStartLoading() 
	{
		if (takeContentChanged() || cachedResults == null || System.currentTimeMillis() - lastUpdate > CACHE_VALIDITY)
		{
			forceLoad();
		}
		else
		{
			deliverResult(cachedResults);
		}
	}
	
}
