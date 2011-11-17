package si.virag.bicikel.map;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import si.virag.bicikel.R;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class StationInfoTouchHandler implements OnTouchListener 
{
	private MapActivity context;
	private GoogleAnalyticsTracker tracker;
	
	private Drawable defaultBackground;
	private Drawable selectedBackground;
	
	public StationInfoTouchHandler(MapActivity context, GoogleAnalyticsTracker tracker)
	{
		this.context = context;
		this.tracker = tracker;
		
		this.selectedBackground = context.getResources().getDrawable(R.drawable.info_select);
	}
	
	@Override
	public boolean onTouch(View parent, MotionEvent motionEvent) 
	{
		switch(motionEvent.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				if (defaultBackground == null)
					defaultBackground = parent.getBackground();
				
				parent.setBackgroundDrawable(selectedBackground);
				break;
			case MotionEvent.ACTION_UP:
				parent.setBackgroundDrawable(defaultBackground);
				if ((context.getSelectedLat() < 0.1) || (context.getSelectedLng() < 0.1))
				{
					return true;
				}
			
				startMaps(context.getSelectedLat(), context.getSelectedLng(), context.getMyLocationLat(), context.getMyLocationLng());
				break;
			default:
				break;
		}
		
		return true;
	}

	
	private void startMaps(double lat, double lng, double myLat, double myLng)
	{
		if (tracker != null)
			tracker.trackEvent("MapView", "StationTap", "Station", 0);
		
		Uri mapsUri;
		if (myLat > 0.1 && myLng > 0.1)
		{
			mapsUri = Uri.parse("http://maps.google.com/maps?dirflg=w&saddr=" + myLat + ", " + myLng + "&daddr=" + lat + "," + lng);
		}
		else
		{
			mapsUri = Uri.parse("http://maps.google.com/maps?dirflg=w&daddr=" + lat + "," + lng);
		}
		
		Log.i(this.toString(), "Opening maps with " + mapsUri);
		
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, mapsUri);
		// This prevents app selection pop-up
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		context.startActivity(intent);
	}
}
