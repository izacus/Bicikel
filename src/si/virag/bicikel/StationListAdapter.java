package si.virag.bicikel;

import java.util.List;

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
    		view.setTag(viewHolder);
		}
		else
		{
			viewHolder = (StationViewHolder)view.getTag();
		}
		
		viewHolder.bikeNum.setText(String.valueOf(stations.get(position).getAvailableBikes()));
		viewHolder.freeSpaces.setText(String.valueOf(stations.get(position).getFreeSpaces()));
		viewHolder.stationName.setText(stations.get(position).getName());
		
		return view;
	}
}
