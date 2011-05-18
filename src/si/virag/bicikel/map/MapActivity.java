package si.virag.bicikel.map;

import java.util.List;

import si.virag.bicikel.R;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class MapActivity extends com.google.android.maps.MapActivity
{
	private MapView mapView;
	private MyLocationOverlay myLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		Bundle extras = getIntent().getExtras();
		double lat = extras.getDouble("lng");
		double lng = extras.getDouble("lat");
		
		mapView = (MapView) findViewById(R.id.map);
		GeoPoint pt = new GeoPoint((int)(lng * 1E6), (int)(lat * 1E6));
		
		MapController controller = mapView.getController();
		controller.setCenter(pt);
		controller.setZoom(17);
		
		List<Overlay> overlays = mapView.getOverlays();
		overlays.clear();
		
		// Add station overlay
		IconOverlay overlay = new IconOverlay(getResources(), pt, R.drawable.cycling);
		overlays.add(overlay);
		
		// Add user location overlay
		myLocation = new MyLocationOverlay(this, mapView);
		myLocation.enableCompass();
		myLocation.enableMyLocation();
		overlays.add(myLocation);
		
		mapView.invalidate();
		mapView.setBuiltInZoomControls(true);
		
		Log.i(this.toString(), "Showing lat" + lat + ", lng " + lng);
		
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
