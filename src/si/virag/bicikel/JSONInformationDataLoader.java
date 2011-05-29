package si.virag.bicikel;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import si.virag.bicikel.data.Station;
import si.virag.bicikel.data.StationInfo;
import si.virag.bicikel.util.AsyncLoader;
import si.virag.bicikel.util.HTTPHelper;
import android.content.Context;
import android.util.Log;

public class JSONInformationDataLoader extends AsyncLoader<StationInfo>
{
	private static final String STATION_LIST_URL = "http://prevoz.org/api/bicikelj/list/";
	
	public JSONInformationDataLoader(Context context)
	{
		super(context);
	}

	@Override
	public StationInfo loadInBackground()
	{
		Log.i(this.toString(), "Loading station data from server...");
		
		String response = "";
		
		try
		{
			response = HTTPHelper.httpGet(STATION_LIST_URL);
			Log.d(this.toString(), "Data received.");
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
				
				Log.d(this.toString(), "Station " + station.getName() + " added.");
			}
			
			return info;
		}
		catch (JSONException e)
		{
			Log.e(this.toString(), "Error parsing response JSON.", e);
			return null;
		}
	}

	
	
}
