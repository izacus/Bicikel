package si.virag.bicikel.map;


import java.util.ArrayList;
import java.util.List;

import si.virag.bicikel.R;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StationOverlay extends ItemizedOverlay<OverlayItem>
{
	private View infoView;
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private List<StationMarker> markers;
	
	private TextView stationName;
	private TextView numBikes;
	private TextView freeSpaces;
	
	public StationOverlay(View infoView, Drawable marker, List<StationMarker> markers)
	{
		super(marker);
	
		this.infoView = infoView;
		this.markers = markers;
		
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
		//Toast.makeText(context, items.get(index).getTitle(), Toast.LENGTH_SHORT).show();
		stationName.setText(items.get(index).getTitle());
		numBikes.setText(String.valueOf(markers.get(index).getBikes()));
		freeSpaces.setText(String.valueOf(markers.get(index).getFree()));
		
		infoView.setVisibility(View.VISIBLE);
		
		
		return true;
	}

	@Override
	public int size()
	{
		return items.size();
	}
}
