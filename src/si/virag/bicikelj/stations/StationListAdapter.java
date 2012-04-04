package si.virag.bicikelj.stations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.util.DisplayUtils;
import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StationListAdapter extends BaseAdapter
{	
	private static class StationViewHolder
	{
		public TextView bikeNum;
		public TextView freeSpaces;
		public TextView stationName;
		public TextView distance;
	}
	
	private Activity context;
	private List<Station> items;
	
	public StationListAdapter(Activity context, int textViewResourceId, List<Station> items)
	{
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		StationViewHolder viewHolder;
		
		if (view == null)
		{
            LayoutInflater li = context.getLayoutInflater();
            view = li.inflate(R.layout.stationlist_item, null);
            
            viewHolder = new StationViewHolder();
    		viewHolder.bikeNum = (TextView) view.findViewById(R.id.stationlist_num_full);
    		viewHolder.freeSpaces = (TextView) view.findViewById(R.id.stationlist_num_free);
    		viewHolder.stationName = (TextView) view.findViewById(R.id.stationlist_name);
    		viewHolder.distance = (TextView) view.findViewById(R.id.stationlist_distance);
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
			viewHolder.distance.setText(DisplayUtils.formatDistance(getItem(position).getDistance()));
			viewHolder.distance.setVisibility(View.VISIBLE);
		}
		else
		{
			viewHolder.distance.setVisibility(View.GONE);
		}
		
		return view;
	}

	
	public void updateData(StationInfo info)
	{
		if (info == null) {
			this.items.clear();
			return;
		}
		
		if (info.getStations().size() != this.getCount())
		{
			this.items = info.getStations();
			return;
		}
		
		for (Station station : info.getStations())
		{
			for (int i = 0; i < this.getCount(); i++)
			{
				if (getItem(i).getId() == station.getId())
				{
					getItem(i).setFreeSpaces(station.getFreeSpaces());
					getItem(i).setAvailableBikes(station.getAvailableBikes());
					break;
				}
			}
		}
	}
	
	public void updateLocation(Location location)
	{
		for (Station station : items)
		{
			station.setDistance(location);
		}
		
		Collections.sort(items, new Comparator<Station>()
		{
			@Override
			public int compare(Station lhs, Station rhs)
			{
				return lhs.getDistance().compareTo(rhs.getDistance());
			}
		});
	}
	
	public void clearData()
	{
		this.items.clear();
	}

	@Override
	public int getCount() 
	{
		return this.items.size();
	}

	@Override
	public Station getItem(int position) 
	{
		if (position < 0 && position >= this.items.size())
			throw new IndexOutOfBoundsException();
		return this.items.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		if (position < 0 && position >= this.items.size())
			throw new IndexOutOfBoundsException();
		return this.items.get(position).getId();
	}
}
