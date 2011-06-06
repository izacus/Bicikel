package si.virag.bicikel.map;

import java.util.ArrayList;
import java.util.List;

import si.virag.bicikel.R;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		tracker = GoogleAnalyticsTracker.getInstance();
		
		mapView = (MapView) findViewById(R.id.map);
		infoView = (View) findViewById(R.id.station_info);
		infoView.setVisibility(View.GONE);
		
		Bundle extras = getIntent().getExtras();
		double[] lats = extras.getDoubleArray("lng");
		double[] lngs = extras.getDoubleArray("lat");
		String[] names = extras.getStringArray("names");
		int[] free = extras.getIntArray("free");
		int[] bikes = extras.getIntArray("bikes");
		
		List<Overlay> overlays = prepareOverlays(lats, lngs, names, free, bikes);
		
		// If we're showing only one point, center on it
		if (lats.length == 1)
		{
			GeoPoint pt = new GeoPoint((int)(lngs[0] * 1E6), (int)(lats[0] * 1E6));
			MapController controller = mapView.getController();
			controller.setCenter(pt);
			controller.setZoom(16);
			
			TextView nameView = (TextView)infoView.findViewById(R.id.txt_station_name);
			TextView freeView = (TextView)infoView.findViewById(R.id.txt_freenum);
			TextView bikesView = (TextView)infoView.findViewById(R.id.txt_bikenum);
			
			nameView.setText(names[0]);
			freeView.setText(String.valueOf(free[0]));
			bikesView.setText(String.valueOf(bikes[0]));
			
			if (free[0] == 0)
			{
				infoView.setBackgroundColor(getResources().getColor(R.color.full_background));
			}
			else if (bikes[0] == 0)
			{
				infoView.setBackgroundColor(getResources().getColor(R.color.empty_background));
			}
			
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
		mapView.setBuiltInZoomControls(true);
	}

	/**
	 * Prepares overlays with station icons
	 */
	private List<Overlay> prepareOverlays(double[] lats, double[] lngs, String[] names, int[] free, int[] bikes)
	{
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
		
		overlays.add(new StationOverlay(this, infoView, tracker, marker, normalMarkers));
		
		
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
		
		overlays.add(new StationOverlay(this, infoView, tracker, fullMarker, fullMarkers));
		
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
		
		overlays.add(new StationOverlay(this, infoView, tracker, emptyMarker, emptyMarkers));
		
		return overlays;
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
		if (showingWholeMap)
		{
			tracker.trackPageView("/MapView/whole");
		}
		else
		{
			tracker.trackPageView("/MapView/single");
		}

		myLocation.enableCompass();
		myLocation.enableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

}
