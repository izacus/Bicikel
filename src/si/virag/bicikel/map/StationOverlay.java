package si.virag.bicikel.map;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StationOverlay extends ItemizedOverlay<OverlayItem>
{
	private Context context;
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	
	public StationOverlay(Context context, Drawable marker, List<StationMarker> markers)
	{
		super(marker);
		
		this.context = context;
		
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
		Toast.makeText(context, items.get(index).getTitle(), Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public int size()
	{
		return items.size();
	}
}
