package si.virag.bicikelj.station_map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

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
    private boolean detailDisplayed = false;
    // Disable tap while animating
    private boolean tapDisabled = false;
    
    private Double selectedLat = null;
    private Double selectedLng = null;
    
    private Location myLocation;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		prepareInterface();
		
        Bundle extras = getIntent().getExtras();
        double[] longtitudes = extras.getDoubleArray("lngs");
        double[] latitudes = extras.getDoubleArray("lats");
        String[] names = extras.getStringArray("names");
        int[] free = extras.getIntArray("frees");
        int[] bikes = extras.getIntArray("fulls");
        
        // Restore state
        if (savedInstanceState != null)
        {
        	if (savedInstanceState.containsKey("detailDisplayed"))
        	{
        		detailDisplayed = savedInstanceState.getBoolean("detailDisplayed");
        	}
        	
        	if (savedInstanceState.containsKey("lat"))
        	{
        		selectedLat = savedInstanceState.getDouble("lat");
        		selectedLng = savedInstanceState.getDouble("lng");
        	}
        }
        
        List<Overlay> overlays = prepareOverlays(longtitudes, latitudes, names, free, bikes);
        setupMap(overlays);
        
	    if (longtitudes.length == 1)
	    {
        	detailDisplayed = true;
        	setSelectedStation(names[0], 
        					   free[0] == 0 ? "-" : String.valueOf(free[0]), 
        					   bikes[0] == 0 ? "-" : String.valueOf(bikes[0]), 
        					   latitudes[0], 
        					   longtitudes[0]);
        	
            GeoPoint pt = new GeoPoint((int)(latitudes[0] * 1E6), (int)(longtitudes[0] * 1E6));
            MapController controller = mapView.getController();
            controller.setCenter(pt);
            controller.setZoom(16);
        }
		else
		{
			// Show whole map
	        GeoPoint pt = new GeoPoint(MAP_CENTER_LAT, MAP_CENTER_LNG);
	        MapController controller = mapView.getController();
	        controller.setCenter(pt);
	        controller.setZoom(15);
		}
        
        // Hide detail view off-screen
        if (!detailDisplayed)
        {
	        detail.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() 
	        {
				@Override
				public void onGlobalLayout() 
				{
					detail.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					ObjectAnimator animator = ObjectAnimator.ofFloat(detail, "translationY", detail.getHeight());
					animator.setDuration(0);
					animator.start();
				}
			});
        }
	}

	private void setupMap(List<Overlay> overlays) 
	{
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
                        }
                }
        });
        
        overlays.add(myLocationOverlay);
        mapView.invalidate();
        mapView.setBuiltInZoomControls(false);
	}

	private void prepareInterface() {
		mapView = (MapView)findViewById(R.id.map_view);
		detail = findViewById(R.id.maps_detail);
		detailFull = (TextView)findViewById(R.id.maps_num_full);
		detailFree = (TextView)findViewById(R.id.maps_num_free);
		detailName = (TextView)findViewById(R.id.maps_name);
		detailDistance = (TextView)findViewById(R.id.maps_distance);
	}
	
	
	
	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
		FlurryAgent.logEvent("MapBackButtonTap");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				FlurryAgent.logEvent("MapBackHomeTap");
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
                    		if (tapDisabled)
                    			return true;
                    		
                            Bundle data = msg.getData();
                            String name = data.getString("name");
                            String free = data.getString("freeSpaces");
                            String bikes = data.getString("numBikes");
                            double lat = data.getDouble("lat");
                            double lng = data.getDouble("lng");
                            
                    		Map<String, String> params = new HashMap<String, String>();
                    		params.put("Station", name);
                    		FlurryAgent.logEvent("MapStationMarkerTap");
                            
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
	
	private void setSelectedStation(final String name, final String free, final String bikes, double lat, double lng) 
	{
		selectedLat = lat;
		selectedLng = lng;
		
		final Float distance;
		if (myLocation != null)
		{
			Location loc = new Location("");
			loc.setLatitude(lat);
			loc.setLongitude(lng);
			distance = myLocation.distanceTo(loc);
		}
		else
		{
			distance = null;
		}
		
		if (!detailDisplayed)
		{
			setDetailsText(name, free, bikes, distance);
			// Pop-in details
			ObjectAnimator animator = ObjectAnimator.ofFloat(detail, "translationY", 0);
	        animator.setInterpolator(new DecelerateInterpolator());
			animator.start();
		}
		else 
		{
			tapDisabled = true;
			final AnimatorSet fadeIn = new AnimatorSet();
			final ObjectAnimator fadeInText = ObjectAnimator.ofFloat(detailName, "alpha", 0, 255);
			final ObjectAnimator fadeInFree = ObjectAnimator.ofFloat(detailFree, "alpha", 0, 255);
			final ObjectAnimator fadeInFull = ObjectAnimator.ofFloat(detailFull, "alpha", 0, 255);
			final ObjectAnimator fadeInDistance = ObjectAnimator.ofFloat(detailDistance, "alpha", 0, 255);
			fadeIn.setInterpolator(new AccelerateInterpolator());
			fadeIn.play(fadeInText).with(fadeInFree);
			fadeIn.play(fadeInFree).with(fadeInFull);
			fadeIn.play(fadeInFull).with(fadeInDistance);
			
			final AnimatorSet fadeOut = new AnimatorSet();
			final ObjectAnimator fadeOutText = ObjectAnimator.ofFloat(detailName, "alpha", 255, 0);
			final ObjectAnimator fadeOutFree = ObjectAnimator.ofFloat(detailFree, "alpha", 255, 0);
			final ObjectAnimator fadeOutFull = ObjectAnimator.ofFloat(detailFull, "alpha", 255, 0);
			final ObjectAnimator fadeOutDistance = ObjectAnimator.ofFloat(detailDistance, "alpha", 255, 0);
			fadeOut.setInterpolator(new AccelerateInterpolator());
			fadeOut.play(fadeOutText).with(fadeOutFree);
			fadeOut.play(fadeOutFree).with(fadeOutFull);
			fadeOut.play(fadeOutFull).with(fadeOutDistance);
			
			fadeOut.addListener(new AnimatorListener() 
			{
				@Override
				public void onAnimationEnd(Animator animation) {
					setDetailsText(name, free, bikes, distance);
					fadeIn.start();
				}

				@Override
				public void onAnimationStart(Animator animation) {}

				@Override
				public void onAnimationCancel(Animator animation) {}

				@Override
				public void onAnimationRepeat(Animator animation) {}
			});
			
			fadeIn.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {}
				
				@Override
				public void onAnimationRepeat(Animator animation) {}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					tapDisabled = false;
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {}
			});
			
			fadeOut.start();
		}
		
		detailDisplayed = true;
	}

	private void setDetailsText(String name, String free, String bikes, Float distance) {
		detailName.setText(name);
		detailFull.setText(bikes);
		detailFree.setText(free);
		
		if (distance != null)
		{
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
	
    @Override
	protected void onSaveInstanceState(Bundle outState) 
    {
		super.onSaveInstanceState(outState);
		if (selectedLat != null) {
			outState.putDouble("lat", selectedLat);
		}
		
		if (selectedLng != null) {
			outState.putDouble("lng", selectedLng);
		}
		
		outState.putBoolean("detailDisplayed", detailDisplayed);
			
	}

	private static Location E6ToLocation(int latitudeE6, int longtitudeE6)
    {
            Location loc = new Location("");
            
            loc.setLatitude((double)latitudeE6 / (double)1E6);
            loc.setLongitude((double)longtitudeE6 / (double)1E6);
            
            return loc;
    }

	@Override
	protected void onStop() 
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		FlurryAgent.setUseHttps(true);	// Don't send users data in plain text
		FlurryAgent.setReportLocation(false);	// Don't report users location for stats, not needed
		FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
	}
	
	
}
