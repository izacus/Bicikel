package si.virag.bicikel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import si.virag.bicikel.data.Station;
import si.virag.bicikel.data.StationInfo;
import si.virag.bicikel.map.MapActivity;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<StationInfo>
{
	private static final int INFO_LOADER_ID = 1;
	
	private static final int MENU_REFRESH = 1;
	private static final int MENU_ABOUT = 2;
	
	private ViewFlipper viewFlipper;
	private ListView stationList;
	private TextView loadingText;
	private ProgressBar throbber;
	private ImageButton mapButton;
	
	private StationInfo stationInfo;
	private StationListAdapter stationInfoAdapter;
	private Location currentLocation;
	
	private GPSManager gpsManager;
	private Handler gpsLocationHandler;
	
	private boolean loadInProgress;
	private boolean waitingForLocation;
	
	private Long lastUpdate;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        if (savedInstanceState != null)
        {
        	lastUpdate = savedInstanceState.getLong("lastupdate");
        }
        
        viewFlipper = (ViewFlipper) findViewById(R.id.main_flipper);
        stationList = (ListView) findViewById(R.id.station_list);
        loadingText = (TextView) findViewById(R.id.txt_loading);
        throbber = (ProgressBar) findViewById(R.id.loading_progress);
        mapButton = (ImageButton) findViewById(R.id.map_button);
        
        stationInfoAdapter = new StationListAdapter(this, R.layout.station_list_item, new ArrayList<Station>());
        stationList.setAdapter(stationInfoAdapter);
        
        mapButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				 showAllStationsMap();
			}
		});
        
        // Set view flipper animations
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        
        viewFlipper.setDisplayedChild(0);
        
        // Setup delayed GPS location handler
        gpsManager = new GPSManager();
        gpsLocationHandler = new Handler()
        {
			@Override
			public void handleMessage(Message msg)
			{
				Log.i(this.toString(), "Location update received...");
				
				if (msg.what == GPSManager.GPS_LOCATION_OK && waitingForLocation)
				{
					sortDataByLocation();
				}
				
				gpsManager.cancelSearch();
			}
        };
        
        gpsManager.findCurrentLocation(this, gpsLocationHandler);        
        loadingText.setText(getString(R.string.loading));
        throbber.setVisibility(View.VISIBLE);
        
        loadInProgress = true;
        waitingForLocation = true;
        
        getSupportLoaderManager().initLoader(INFO_LOADER_ID, null, this);
        
    }
    
	@Override
	public Loader<StationInfo> onCreateLoader(int id, Bundle args)
	{
		JSONInformationDataLoader infoLoader = new JSONInformationDataLoader(this);
		return infoLoader;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		if (lastUpdate != null)
			outState.putLong("lastupdate", lastUpdate);
	}

	@Override
	public void onLoadFinished(Loader<StationInfo> loader, StationInfo result)
	{
		Log.i(this.toString(), "Loading done.");
		loadInProgress = false;
		stationInfo = result;
		lastUpdate = Calendar.getInstance().getTimeInMillis();
		
		// Check for error
		if (result == null)
		{
			throbber.setVisibility(View.GONE);
			loadingText.setText(getString(R.string.connection_error));
			loadingText.setGravity(Gravity.CENTER_HORIZONTAL);
			return;
		}
		
		currentLocation = gpsManager.getCurrentLocation();
		
		if (currentLocation != null)
		{
			stationInfo.calculateDistances(currentLocation);
			waitingForLocation = false;
			gpsManager.cancelSearch();
		}
		
		stationInfoAdapter.clear();
		for (Station station : stationInfo.getStations())
		{
			stationInfoAdapter.add(station);
		}
		
		stationInfoAdapter.notifyDataSetChanged();
		viewFlipper.showNext();
		
		stationList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				Station station = stationInfo.getStations().get(position);
				
				Intent newActivity = new Intent(MainActivity.this, MapActivity.class);
				
				double[] longtitudes = new double[] { station.getLocation().getLongitude() };
				double[] latitudes = new double[] { station.getLocation().getLatitude() };
				String[] names = new String[] { station.getName() };
				int[] free = new int[] { station.getFreeSpaces() };
				int[] bikes = new int[] { station.getAvailableBikes() };
				
				
				newActivity.putExtra("lng", longtitudes);
				newActivity.putExtra("lat", latitudes);
				newActivity.putExtra("names", names);
				newActivity.putExtra("free", free);
				newActivity.putExtra("bikes", bikes);
				
				startActivity(newActivity);
			}
		});
	}
	
	@Override
	public void onLoaderReset(Loader<StationInfo> loader)
	{
		// Nothing TBD
	}
    
	private void refreshData()
	{
		Log.w(this.toString(), "Forcing data refresh...");
		
		viewFlipper.setDisplayedChild(0);
		waitingForLocation = true;
		loadInProgress = true;
        gpsManager.findCurrentLocation(this, gpsLocationHandler);        
        loadingText.setText(getString(R.string.loading));
        throbber.setVisibility(View.VISIBLE);               
        getSupportLoaderManager().getLoader(INFO_LOADER_ID).forceLoad();
	}
	
	
	private void sortDataByLocation()
	{
		// We might not yet have valid data
		if (stationInfo == null)
			return;
		
		Location currentLocation = gpsManager.getCurrentLocation();
		
		if (currentLocation != null)
		{
			waitingForLocation = false;
			stationInfo.calculateDistances(currentLocation);
			
			stationInfoAdapter.sort(new Comparator<Station>()
			{
				@Override
				public int compare(Station object1, Station object2)
				{
					return object1.getDistance().compareTo(object2.getDistance());
				}
			});
			
			stationInfoAdapter.notifyDataSetChanged();
		}
	}
	
	private void showAllStationsMap()
	{
		
		Intent newActivity = new Intent(MainActivity.this, MapActivity.class);
		
		List<Station> stations = stationInfo.getStations();
		
		double[] longtitudes = new double[stations.size()];
		double[] latitudes = new double[stations.size()];
		String[] names = new String[stations.size()];
		int[] freePlaces = new int[stations.size()];
		int[] bikes = new int[stations.size()];
		
		int i = 0;
		
		for (Station station : stations)
		{
			longtitudes[i] = station.getLocation().getLongitude();
			latitudes[i] = station.getLocation().getLatitude();
			names[i] = station.getName();
			freePlaces[i] = station.getFreeSpaces();
			bikes[i] = station.getAvailableBikes();
			i++;
		}
		
		newActivity.putExtra("lng", longtitudes);
		newActivity.putExtra("lat", latitudes);
		newActivity.putExtra("names", names);
		newActivity.putExtra("free", freePlaces);
		newActivity.putExtra("bikes", bikes);
		
		startActivity(newActivity);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (lastUpdate != null && lastUpdate != 0 && Calendar.getInstance().getTimeInMillis() - lastUpdate > 5 * 60 * 1000)
		{
			refreshData();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case MENU_ABOUT:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(getString(R.string.app_name) + " " + getString(R.string.app_ver));
				builder.setMessage(getString(R.string.app_about));
				
				AlertDialog alert = builder.create();
				alert.show();
				break;
				
			case MENU_REFRESH:
				if (!loadInProgress)
				{
					this.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							refreshData();
						}
					});
				}
				break;
		}
		
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, MENU_REFRESH, 0, getString(R.string.menu_refresh)).setIcon(R.drawable.refresh);
		menu.add(0, MENU_ABOUT, 1, getString(R.string.menu_about)).setIcon(R.drawable.info);
		return super.onCreateOptionsMenu(menu);
	}
    
}