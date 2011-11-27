package si.virag.bicikelj.map;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StationOverlay extends ItemizedOverlay<OverlayItem>
{
	private Handler tapNotifier;
	
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private List<StationMarker> markers;
	
	private GoogleAnalyticsTracker tracker;
	
	public StationOverlay(Context context, 
						  Handler tapNotifier, 
						  GoogleAnalyticsTracker tracker, 
						  Drawable marker, 
						  List<StationMarker> markers)
	{
		super(marker);
		
		this.tracker = tracker;
		this.markers = markers;
		this.tapNotifier = tapNotifier;
		
		for (StationMarker station : markers)
		{
			boundCenterBottom(marker);
			items.add(new OverlayItem(new GeoPoint((int)(station.getLatitude() * 1E6), 
					 							   (int)(station.getLongtitude() * 1E6)), 
					 							   station.getDescription(), ""));
		}
		
		populate();		
	}
	
	@Override
	protected OverlayItem createItem(int i)
	{
		return items.get(i);
	}
	
	@Override
	protected boolean onTap(int index)
	{
		int bikeNumber = markers.get(index).getBikes();
		int freeNumber = markers.get(index).getFree();
		
		tracker.trackEvent("MapView", "MarkerTap", items.get(index).getTitle(), 0);
		
		Bundle tapped = new Bundle();
		tapped.putString("name", items.get(index).getTitle());
		tapped.putString("numBikes", bikeNumber == 0 ? "-" : String.valueOf(bikeNumber));
		tapped.putString("freeSpaces", freeNumber == 0 ? "-" : String.valueOf(freeNumber));
		tapped.putDouble("lat", markers.get(index).getLatitude());
		tapped.putDouble("lng", markers.get(index).getLongtitude());
		Message msg = new Message();
		msg.setData(tapped);
		tapNotifier.sendMessage(msg);
		
		return true;
	}

	@Override
	public int size()
	{
		return items.size();
	}
}
