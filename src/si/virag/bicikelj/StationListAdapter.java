package si.virag.bicikelj;

import java.util.List;
import java.util.Locale;

import si.virag.bicikelj.data.Station;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class StationListAdapter extends ArrayAdapter<Station>
{	
	private static class StationViewHolder
	{
		public TextView bikeNum;
		public TextView freeSpaces;
		public TextView stationName;
		public TextView distance;
		public LinearLayout free_bar;
	}
	
	private Activity context;
	
	public StationListAdapter(Activity context, int textViewResourceId, List<Station> items)
	{
		super(context, textViewResourceId, items);
		this.context = context;
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
    		viewHolder.free_bar = (LinearLayout) view.findViewById(R.id.free_bar);
    		view.setTag(viewHolder);
		}
		else
		{
			viewHolder = (StationViewHolder)view.getTag();
		}
		Station station = getItem(position);
		
		viewHolder.bikeNum.setText(station.getAvailableBikes() == 0 ? "-" : String.valueOf(station.getAvailableBikes()));
		viewHolder.freeSpaces.setText(station.getFreeSpaces() == 0 ? "-" : String.valueOf(station.getFreeSpaces()));
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
		
		LayoutParams params = (LayoutParams) viewHolder.free_bar.getLayoutParams();
		
		if (station.getAvailableBikes() == 0)
		{
			params.width = parent.getWidth();	// This is here to prevent ugly red dot because of rounding errors
		}
		else
		{
			params.width = (parent.getWidth() / (station.getFreeSpaces() + station.getAvailableBikes())) * getItem(position).getFreeSpaces();
		}
		
		viewHolder.free_bar.setLayoutParams(params);
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
