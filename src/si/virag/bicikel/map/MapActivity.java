package si.virag.bicikel.map;

import java.util.List;

import si.virag.bicikel.R;
import android.location.Location;
import android.os.Bundle;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		mapView = (MapView) findViewById(R.id.map);
		
		Bundle extras = getIntent().getExtras();
		double[] lats = extras.getDoubleArray("lng");
		double[] lngs = extras.getDoubleArray("lat");
		
		// Prepare overlays
		List<Overlay> overlays = mapView.getOverlays();
		overlays.clear();
		
		for (int i = 0; i < lats.length; i++)
		{
			GeoPoint pt = new GeoPoint((int)(lngs[i] * 1E6), (int)(lats[i] * 1E6));
			// Add station overlay
			IconOverlay overlay = new IconOverlay(getResources(), pt, R.drawable.cycling);
			overlays.add(overlay);
		}
		
		
		// If we're showing only one point, center on it
		if (lats.length == 1)
		{
			GeoPoint pt = new GeoPoint((int)(lngs[0] * 1E6), (int)(lats[0] * 1E6));
			MapController controller = mapView.getController();
			controller.setCenter(pt);
			controller.setZoom(17);
		}
		else
		{
			GeoPoint pt = new GeoPoint(MAP_CENTER_LAT, MAP_CENTER_LNG);
			MapController controller = mapView.getController();
			controller.setCenter(pt);
			controller.setZoom(15);
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
				
				if (myLoc.distanceTo(centerLoc) > 10000)
					return;
				
				MapController controller = mapView.getController();
				controller.animateTo(myLocation.getMyLocation());
				controller.setZoom(16);
			}
		});
		
		overlays.add(myLocation);
		
		mapView.invalidate();
		mapView.setBuiltInZoomControls(true);
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
		myLocation.enableCompass();
		myLocation.enableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

}
