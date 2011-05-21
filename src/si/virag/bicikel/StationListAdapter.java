package si.virag.bicikel;

import java.util.List;
import java.util.Locale;

import si.virag.bicikel.data.Station;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StationListAdapter extends ArrayAdapter<Station>
{
	private Activity context;
	private List<Station> stations;
	
	private static class StationViewHolder
	{
		public TextView bikeNum;
		public TextView freeSpaces;
		public TextView stationName;
		public TextView distance;
	}
	
	public StationListAdapter(Activity context, int textViewResourceId, List<Station> items)
	{
		super(context, textViewResourceId, items);
		this.context = context;
		this.stations = items;
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
		
		viewHolder.bikeNum.setText(String.valueOf(stations.get(position).getAvailableBikes()));
		viewHolder.freeSpaces.setText(String.valueOf(stations.get(position).getFreeSpaces()));
		viewHolder.stationName.setText(stations.get(position).getName().replaceAll("-", "\n"));
		
		if (stations.get(position).getDistance() != null)
		{
			viewHolder.distance.setText(formatDistance(stations.get(position).getDistance()));
			viewHolder.distance.setVisibility(View.VISIBLE);
		}
		else
		{
			viewHolder.distance.setVisibility(View.GONE);
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
