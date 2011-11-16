package si.virag.bicikel.map;


import java.util.ArrayList;
import java.util.List;

import si.virag.bicikel.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StationOverlay extends ItemizedOverlay<OverlayItem>
{
	private View infoView;
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private List<StationMarker> markers;
	
	private GoogleAnalyticsTracker tracker;
	
	private TextView stationName;
	private TextView numBikes;
	private TextView freeSpaces;
	
	public StationOverlay(Context context, View infoView, GoogleAnalyticsTracker tracker, Drawable marker, List<StationMarker> markers)
	{
		super(marker);

		this.infoView = infoView;
		this.markers = markers;
		this.tracker = tracker;
		
		stationName = (TextView) infoView.findViewById(R.id.txt_station_name);
		numBikes = (TextView)infoView.findViewById(R.id.txt_bikenum);
		freeSpaces = (TextView)infoView.findViewById(R.id.txt_freenum);
		
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
		
		stationName.setText(items.get(index).getTitle());
		numBikes.setText(bikeNumber == 0 ? "-" : String.valueOf(bikeNumber));
		freeSpaces.setText(freeNumber == 0 ? "-" : String.valueOf(freeNumber));
		infoView.setVisibility(View.VISIBLE);

		return true;
	}

	@Override
	public int size()
	{
		return items.size();
	}
}
