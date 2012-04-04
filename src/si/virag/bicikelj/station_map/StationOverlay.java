package si.virag.bicikelj.station_map;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StationOverlay extends ItemizedOverlay<OverlayItem>
{
	private Handler tapNotifier;
	
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private List<StationMarker> markers;
	
	public StationOverlay(Context context, 
						  Handler tapNotifier, 
						  Drawable marker, 
						  List<StationMarker> markers)
	{
		super(marker);
		
		this.markers = markers;
		this.tapNotifier = tapNotifier;
		
		for (StationMarker station : markers)
		{
			boundCenterBottom(marker);
			items.add(new OverlayItem(new GeoPoint((int)(station.getLocation().getLatitude() * 1E6), 
					 							   (int)(station.getLocation().getLongitude() * 1E6)), 
					 							   String.valueOf(station.getId()), ""));
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
		int stationId = markers.get(index).getId();
		
		Bundle tapped = new Bundle();
		tapped.putInt("stationId", stationId);
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