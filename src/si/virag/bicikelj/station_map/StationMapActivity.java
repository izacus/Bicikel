package si.virag.bicikelj.station_map;

import java.util.ArrayList;
import java.util.List;

import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.util.DisplayUtils;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class StationMapActivity extends SherlockMapActivity 
{
    private static final int MAP_CENTER_LAT = 46051367;
    private static final int MAP_CENTER_LNG = 14506542;
	
	private MapView mapView;
    private MyLocationOverlay myLocationOverlay;
	
    // Detail displays
    private View detail;
    private TextView detailFull;
    private TextView detailFree;
    private TextView detailDistance;
    private TextView detailName;
    
    private Location myLocation;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mapView = (MapView)findViewById(R.id.map_view);
		detail = findViewById(R.id.maps_detail);
		detailFull = (TextView)findViewById(R.id.maps_num_full);
		detailFree = (TextView)findViewById(R.id.maps_num_free);
		detailName = (TextView)findViewById(R.id.maps_name);
		detailDistance = (TextView)findViewById(R.id.maps_distance);
		
        Bundle extras = getIntent().getExtras();
        double[] longtitudes = extras.getDoubleArray("lngs");
        double[] latitudes = extras.getDoubleArray("lats");
        String[] names = extras.getStringArray("names");
        int[] free = extras.getIntArray("frees");
        int[] bikes = extras.getIntArray("fulls");
        
        List<Overlay> overlays = prepareOverlays(longtitudes, latitudes, names, free, bikes);
        
        // Show whole map
        GeoPoint pt = new GeoPoint(MAP_CENTER_LAT, MAP_CENTER_LNG);
        MapController controller = mapView.getController();
        controller.setCenter(pt);
        controller.setZoom(15);
        
        // Add user location overlay
        myLocationOverlay = new MyLocationOverlay(this, mapView);
        myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        
        myLocationOverlay.runOnFirstFix(new Runnable()
        {
                @Override
                public void run()
                {
                        // Make sure we don't scroll away from the city
                        Location centerLoc = E6ToLocation(MAP_CENTER_LAT, MAP_CENTER_LNG);
                        Location myLoc = E6ToLocation(myLocationOverlay.getMyLocation().getLatitudeE6(), 
                                                      myLocationOverlay.getMyLocation().getLongitudeE6());
                        
                        myLocation = myLoc;
                        if (myLoc.distanceTo(centerLoc) > 3000)
                                return;
                        
                        MapController controller = mapView.getController();
                        
                        if (myLocationOverlay.getMyLocation() != null)
                        {
                                controller.animateTo(myLocationOverlay.getMyLocation());
                                controller.setZoom(16);
                        }
                }
        });
        
        overlays.add(myLocationOverlay);
        
        mapView.invalidate();
        mapView.setBuiltInZoomControls(false);
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
    @Override
	protected void onPause() 
    {
		super.onPause();
		myLocationOverlay.disableCompass();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
        myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
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
                            
                            // TODO: distance
                            
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
            
            overlays.add(new StationOverlay(this, tapNotifier, marker, normalMarkers));
            
            
            // Full station markers
            Drawable fullMarker = getResources().getDrawable(R.drawable.cycling); // TODO: fix for color 
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
            
            overlays.add(new StationOverlay(this, tapNotifier, fullMarker, fullMarkers));
            
            // Empty station markers
            Drawable emptyMarker = getResources().getDrawable(R.drawable.cycling); // TODO: fix for color
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
            
            overlays.add(new StationOverlay(this, tapNotifier, emptyMarker, emptyMarkers));
            return overlays;
    }
	
	private void setSelectedStation(String name, String free, String bikes, double lat, double lng) 
	{
		detailName.setText(name);
		detailFull.setText(bikes);
		detailFree.setText(free);
		
		if (myLocation != null)
		{
			Location loc = new Location("");
			loc.setLatitude(lat);
			loc.setLongitude(lng);
			float distance = myLocation.distanceTo(loc);
			
			detailDistance.setText(DisplayUtils.formatDistance(distance));
			detailDistance.setVisibility(View.VISIBLE);
		}
		else
		{
			detailDistance.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
    private static Location E6ToLocation(int latitudeE6, int longtitudeE6)
    {
            Location loc = new Location("");
            
            loc.setLatitude((double)latitudeE6 / (double)1E6);
            loc.setLongitude((double)longtitudeE6 / (double)1E6);
            
            return loc;
    }
}
