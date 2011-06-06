package si.virag.bicikel;

import java.util.List;
import java.util.Locale;

import si.virag.bicikel.data.Station;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StationListAdapter extends ArrayAdapter<Station>
{	
	private static class StationViewHolder
	{
		public TextView bikeNum;
		public TextView freeSpaces;
		public TextView stationName;
		public TextView distance;
	}
	
	private Activity context;
	
	private Drawable defaultBackground;
	private int fullStation;
	private int emptyStation;
	
	public StationListAdapter(Activity context, int textViewResourceId, List<Station> items)
	{
		super(context, textViewResourceId, items);
		this.context = context;
		
		// Get default background color
		LayoutInflater li = context.getLayoutInflater();
		View view = li.inflate(R.layout.station_list_item, null);
		defaultBackground = view.getBackground();
		
		// Get colors for full or empty stations
		fullStation = context.getResources().getColor(R.color.full_background);
		emptyStation = context.getResources().getColor(R.color.empty_background);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		StationViewHolder viewHolder;
		
		if (view == null)
		{
            LayoutInflater li = context.getLayoutInflater();
            view = li.inflate(R.layout.station_list_item, null);
            
            viewHolder = new StationViewHolder();
    		viewHolder.bikeNum = (TextView) view.findViewById(R.id.txt_bikenum);
    		viewHolder.freeSpaces = (TextView) view.findViewById(R.id.txt_freenum);
    		viewHolder.stationName = (TextView) view.findViewById(R.id.txt_station_name);
    		viewHolder.distance = (TextView) view.findViewById(R.id.txt_distance);
    		view.setTag(viewHolder);
		}
		else
		{
			viewHolder = (StationViewHolder)view.getTag();
		}
		
		viewHolder.bikeNum.setText(String.valueOf(getItem(position).getAvailableBikes()));
		viewHolder.freeSpaces.setText(String.valueOf(getItem(position).getFreeSpaces()));
		viewHolder.stationName.setText(getItem(position).getName().replaceAll("-", "\n"));
		
		if (getItem(position).getDistance() != null)
		{
			viewHolder.distance.setText(formatDistance(getItem(position).getDistance()));
			viewHolder.distance.setVisibility(View.VISIBLE);
		}
		else
		{
			viewHolder.distance.setVisibility(View.GONE);
		}
		
		// Colorize background
		if (getItem(position).getAvailableBikes() == 0)
		{
			view.setBackgroundColor(emptyStation);
		}
		else if (getItem(position).getFreeSpaces() == 0)
		{
			view.setBackgroundColor(fullStation);
		}
		else 
		{
			view.setBackgroundDrawable(defaultBackground);
		}
		
		return view;
	}
	
	public String formatDistance(Float distance)
	{
		// Using german locale, because slovene locale is not available on all devices
		// and germany uses same number format
		if (distance < 1200)
		{
			return String.format(Locale.GERMAN, "%,.1f", distance) + " m";
		}
		else
		{
			return String.format(Locale.GERMAN, "%,.2f", distance / 1000) + " km";
		}
	}
	
	
}
