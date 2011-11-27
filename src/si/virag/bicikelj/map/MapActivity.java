package si.virag.bicikelj.map;

import java.util.ArrayList;
import java.util.List;

import si.virag.bicikelj.R;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MapActivity extends com.google.android.maps.MapActivity
{
	private static final int MAP_CENTER_LAT = 46051367;
	private static final int MAP_CENTER_LNG = 14506542;
	
	private static Location E6ToLocation(int latitudeE6, int longtitudeE6)
	{
		Location loc = new Location("");
		
		loc.setLatitude((double)latitudeE6 / (double)1E6);
		loc.setLongitude((double)longtitudeE6 / (double)1E6);
		
		return loc;
	}
	
	private MapView mapView;
	private MyLocationOverlay myLocation;
	private View infoView;
	
	private GoogleAnalyticsTracker tracker;
	
	private boolean showingWholeMap;
	
	private double selectedLat;
	private double selectedLng;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		tracker = GoogleAnalyticsTracker.getInstance();
		
		mapView = (MapView) findViewById(R.id.map);
		
		infoView = (View) findViewById(R.id.station_info);
		infoView.findViewById(R.id.filled_bar).setVisibility(View.INVISIBLE);
		infoView.setVisibility(View.GONE);
		infoView.setOnTouchListener(new StationInfoTouchHandler(this, tracker));
		
		if (savedInstanceState != null)
		{
			selectedLat = savedInstanceState.getDouble("selectedLat", 0);
			selectedLng = savedInstanceState.getDouble("selectedLng", 0);
		}
		
		Bundle extras = getIntent().getExtras();
		double[] longtitudes = extras.getDoubleArray("lng");
		double[] latitudes = extras.getDoubleArray("lat");
		String[] names = extras.getStringArray("names");
		int[] free = extras.getIntArray("free");
		int[] bikes = extras.getIntArray("bikes");
		
		List<Overlay> overlays = prepareOverlays(longtitudes, latitudes, names, free, bikes);
		
		// If we're showing only one point, center on it
		if (longtitudes.length == 1)
		{
			GeoPoint pt = new GeoPoint((int)(latitudes[0] * 1E6), (int)(longtitudes[0] * 1E6));
			selectedLat = latitudes[0];
			selectedLng = longtitudes[0];
			MapController controller = mapView.getController();
			controller.setCenter(pt);
			controller.setZoom(16);
			
			TextView nameView = (TextView)infoView.findViewById(R.id.txt_station_name);
			TextView freeView = (TextView)infoView.findViewById(R.id.txt_freenum);
			TextView bikesView = (TextView)infoView.findViewById(R.id.txt_bikenum);
			
			nameView.setText(names[0]);
			freeView.setText(free[0] == 0 ? "-" : String.valueOf(free[0]));
			bikesView.setText(bikes[0] == 0 ? "-" : String.valueOf(bikes[0]));
			
			infoView.setVisibility(View.VISIBLE);
			showingWholeMap = false;			
		}
		else
		{
			GeoPoint pt = new GeoPoint(MAP_CENTER_LAT, MAP_CENTER_LNG);
			MapController controller = mapView.getController();
			controller.setCenter(pt);
			controller.setZoom(15);
			showingWholeMap = true;
		}
		
		// Add user location overlay
		myLocation = new MyLocationOverlay(this, mapView);
		myLocation.enableCompass();
		myLocation.enableMyLocation();
		
		myLocation.runOnFirstFix(new Runnable()
		{
			@Override
			public void run()
			{
				// Make sure we don't scroll away from the city
				Location centerLoc = E6ToLocation(MAP_CENTER_LAT, MAP_CENTER_LNG);
				Location myLoc = E6ToLocation(myLocation.getMyLocation().getLatitudeE6(), 
											  myLocation.getMyLocation().getLongitudeE6());
				
				if (myLoc.distanceTo(centerLoc) > 3000)
					return;
				
				MapController controller = mapView.getController();
				
				if (myLocation.getMyLocation() != null)
				{
					controller.animateTo(myLocation.getMyLocation());
					controller.setZoom(16);
				}
			}
		});
		
		overlays.add(myLocation);
		
		mapView.invalidate();
		mapView.setBuiltInZoomControls(false);
	}

	/**
	 * Prepares overlays with station icons
	 */
	private List<Overlay> prepareOverlays(double[] lats, double[] lngs, String[] names, int[] free, int[] bikes)
	{
		Handler tapNotifier = new Handler(new Callback() {
			
			@Override
			public boolean handleMessage(Message msg) 
			{
				Bundle data = msg.getData();
				String name = data.getString("name");
				String free = data.getString("freeSpaces");
				String bikes = data.getString("numBikes");
				double lat = data.getDouble("lat");
				double lng = data.getDouble("lng");
				
				setSelectedStation(name, free, bikes, lat, lng);
				
				return true;
			}
		});
		
		// Prepare overlays
		List<Overlay> overlays = mapView.getOverlays();
		overlays.clear();
		
		// Normal markers
		Drawable marker = getResources().getDrawable(R.drawable.cycling);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		
		List<StationMarker> normalMarkers = new ArrayList<StationMarker>();
		
		for (int i = 0; i < lats.length; i++)
		{
			// Skip full or empty stations
			if (free[i] == 0 || bikes[i] == 0)
				continue;
			
			StationMarker station = new StationMarker(lngs[i], lats[i], names[i], free[i], bikes[i]);
			normalMarkers.add(station);
		}
		
		overlays.add(new StationOverlay(this, tapNotifier, tracker, marker, normalMarkers));
		
		
		// Full station markers
		Drawable fullMarker = getResources().getDrawable(R.drawable.cycling_green);
		fullMarker.setBounds(0, 0, fullMarker.getIntrinsicWidth(), fullMarker.getIntrinsicHeight());
		
		List<StationMarker> fullMarkers = new ArrayList<StationMarker>();
		
		for (int i = 0; i < lats.length; i++)
		{
			// Skip full or empty stations
			if (free[i] != 0)
				continue;
			
			StationMarker station = new StationMarker(lngs[i], lats[i], names[i], free[i], bikes[i]);
			fullMarkers.add(station);
		}
		
		overlays.add(new StationOverlay(this, tapNotifier, tracker, fullMarker, fullMarkers));
		
		// Empty station markers
		Drawable emptyMarker = getResources().getDrawable(R.drawable.cycling_red);
		emptyMarker.setBounds(0, 0, emptyMarker.getIntrinsicWidth(), emptyMarker.getIntrinsicHeight());
		
		List<StationMarker> emptyMarkers = new ArrayList<StationMarker>();
		
		for (int i = 0; i < lats.length; i++)
		{
			// Skip full or empty stations
			if (bikes[i] != 0)
				continue;
			
			StationMarker station = new StationMarker(lngs[i], lats[i], names[i], free[i], bikes[i]);
			emptyMarkers.add(station);
		}
		
		overlays.add(new StationOverlay(this, tapNotifier, tracker, emptyMarker, emptyMarkers));
		
		return overlays;
	}
	
	private void setSelectedStation(String name, 
									String free, 
									String bikes,
									double lat, 
									double lng) 
	{
		selectedLat = lat;
		selectedLng = lng;
		
		TextView nameView = (TextView)infoView.findViewById(R.id.txt_station_name);
		TextView freeView = (TextView)infoView.findViewById(R.id.txt_freenum);
		TextView bikesView = (TextView)infoView.findViewById(R.id.txt_bikenum);
		
		nameView.setText(name);
		freeView.setText(free);
		bikesView.setText(bikes);
		
		infoView.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		myLocation.disableCompass();
		myLocation.disableMyLocation();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (tracker == null)
			tracker = GoogleAnalyticsTracker.getInstance();
		
		try
		{
			if (showingWholeMap)
			{
				tracker.trackPageView("/MapView/whole");
			}
			else
			{
				tracker.trackPageView("/MapView/single");
			}
		}
		catch (Exception e)		// For some reason GAE tracker tends to crash after resume
		{
			Log.e(this.toString(), e.getMessage());
		}

		myLocation.enableCompass();
		myLocation.enableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}
	
	public double getSelectedLng()
	{
		return selectedLng;
	}
	
	public double getSelectedLat()
	{
		return selectedLat;
	}
	
	public double getMyLocationLat()
	{
		if (myLocation.getMyLocation() != null)
			return myLocation.getMyLocation().getLatitudeE6() / (double)1E6;
		
		return 0;
	}
	
	public double getMyLocationLng()
	{
		if (myLocation.getMyLocation() != null)
			return myLocation.getMyLocation().getLongitudeE6() / (double)1E6;
		
		return 0;
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		
		outState.putDouble("selectedLng", selectedLng);
		outState.putDouble("selectedLat", selectedLat);
	}

}
