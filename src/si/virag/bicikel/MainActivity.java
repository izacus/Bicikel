package si.virag.bicikel;

import si.virag.bicikel.data.StationInfo;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ListView;
import android.widget.ViewFlipper;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<StationInfo>
{
	private static final int INFO_LOADER_ID = 1;
	
	private ViewFlipper viewFlipper;
	private ListView stationList;
	
	private StationInfo stationInfo;
	private Location currentLocation;
	
	private GPSManager gpsManager;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        viewFlipper = (ViewFlipper) findViewById(R.id.main_flipper);
        stationList = (ListView) findViewById(R.id.station_list);
        
        gpsManager = new GPSManager();
        gpsManager.findCurrentLocation(this, new Handler());
        
        getSupportLoaderManager().initLoader(INFO_LOADER_ID, null, this);
    }

	@Override
	public Loader<StationInfo> onCreateLoader(int id, Bundle args)
	{
		InformationDataLoader infoLoader = new InformationDataLoader(this);
		return infoLoader;
	}

	@Override
	public void onLoadFinished(Loader<StationInfo> loader, StationInfo result)
	{
		stationInfo = result;
		
		gpsManager.cancelSearch();
		currentLocation = gpsManager.getCurrentLocation();
		
		if (currentLocation != null)
			stationInfo.calculateDistances(currentLocation);
		
		StationListAdapter adapter = new StationListAdapter(this, R.layout.station_list_item, stationInfo.getStations());
		stationList.setAdapter(adapter);
		viewFlipper.showNext();
	}
	
	@Override
	public void onLoaderReset(Loader<StationInfo> loader)
	{
		loader.reset();
	}
    
    
}