package si.virag.bicikel.map;

import si.virag.bicikel.R;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class MapActivity extends com.google.android.maps.MapActivity
{
	private MapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		
		Bundle extras = getIntent().getExtras();
		double lng = extras.getDouble("lng");
		double lat = extras.getDouble("lat");
		
		mapView = (MapView) findViewById(R.id.map);
		GeoPoint pt = new GeoPoint((int)(lng * 1E6), (int)(lat * 1E6));
		
		MapController controller = mapView.getController();
		controller.setCenter(pt);
		
		Log.i(this.toString(), "Showing lat" + lat + ", lng " + lng);
		
	}

	@Override
	protected boolean isRouteDisplayed()
	{
		return false;
	}

}
