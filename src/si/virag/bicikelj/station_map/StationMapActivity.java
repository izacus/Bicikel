package si.virag.bicikelj.station_map;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;

import java.util.ArrayList;

public class StationMapActivity extends SherlockFragmentActivity
{
    private static final double MAP_CENTER_LAT = 46.051367;
    private static final double MAP_CENTER_LNG = 14.506542;

    private SupportMapFragment mapFragment;
	private GoogleMap map;

    // Disable tap while animating
    private Location myLocation;
    
    private ArrayList<Station> stations;
    private int selectedStationId = - 1;
    
    private MenuItem directionsItem = null;
    
    private Station getStationById(int id)
    {
    	for (Station station : stations)
    	{
    		if (station.getId() == id)
    			return station;
    	}
    	
    	return null;
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu_maps, menu);
		
		directionsItem = menu.findItem(R.id.menu_directions);
		
		if (this.selectedStationId < 0) {
			directionsItem.setVisible(false);
		}
		
		return true;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras;
        if (savedInstanceState == null)
        {
            mapFragment = new SupportMapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.map_holder, mapFragment, "MapFragment").commit();
            extras = getIntent().getExtras();
        }
        else
        {
            mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("MapFragment");
            extras = savedInstanceState;
        }

        this.stations = extras.getParcelableArrayList("stations");

        // Restore state
        if (savedInstanceState != null)
        {
        	if (savedInstanceState.containsKey("stationId"))
        	{
        		selectedStationId = savedInstanceState.getInt("stationId");
        	}
        }
    }
	
	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				Intent intent = new Intent(this, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			case R.id.menu_directions:
				showDirections();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void showDirections()
	{
		if (selectedStationId < 0)
			return;
		
		Station selectedStation = getStationById(selectedStationId);
		
		Uri mapsUri;
		if (myLocation == null)
		{
			mapsUri = Uri.parse("http://maps.google.com/maps?dirflg=w&daddr=" + selectedStation.getLocation().getLatitude() + "," + selectedStation.getLocation().getLongitude());
		}
		else
		{
            mapsUri = Uri.parse("http://maps.google.com/maps?dirflg=w&saddr=" + myLocation.getLatitude() + ", " + 
            				    myLocation.getLongitude() + "&daddr=" + selectedStation.getLocation().getLatitude() + "," + 
            				    selectedStation.getLocation().getLongitude());
		}
		
        Log.i(this.toString(), "Opening maps with " + mapsUri);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, mapsUri);
        // This prevents app selection pop-up
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
	}
	
    @Override
	protected void onPause() 
    {
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();

        if (map == null)
        {
            map = mapFragment.getMap();
            if (map != null)
            {
                setupMap();
            }
        }
	}

    private void setupMap()
    {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);

        CameraUpdate update;
        if (stations.size() == 1)
        {
            Station station = stations.get(0);
            update = CameraUpdateFactory.newLatLngZoom(new LatLng(station.getLocation().getLatitude(), station.getLocation().getLongitude()), 16.0f);
        }
        else
        {
            update = CameraUpdateFactory.newLatLngZoom(new LatLng(MAP_CENTER_LAT, MAP_CENTER_LNG), 15.0f);
        }

        map.animateCamera(update);
        createMarkers();
    }

    private void createMarkers()
    {
        for (Station station : stations)
        {
            String stationString = getString(R.string.map_hint, station.getAvailableBikes(), station.getFreeSpaces()); //+ station.getAvailableBikes() + " " + getString(R.string.free) + ": " + station.getFreeSpaces();

            int bikeIcon = R.drawable.cycling;
            if (station.getAvailableBikes() == 0)
                bikeIcon = R.drawable.cycling_free;
            else if (station.getFreeSpaces() == 0)
                bikeIcon = R.drawable.cycling_full;

            map.addMarker(new MarkerOptions().position(new LatLng(station.getLocation().getLatitude(), station.getLocation().getLongitude()))
                                             .title(station.getName())
                                             .snippet(stationString)
                                             .icon(BitmapDescriptorFactory.fromResource(bikeIcon)));
        }
    }
	
    @Override
	protected void onSaveInstanceState(Bundle outState) 
    {
		super.onSaveInstanceState(outState);
		
		outState.putParcelableArrayList("stations", stations);
		if (selectedStationId > 0)
			outState.putInt("stationId", selectedStationId);
		
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
	}	
}
