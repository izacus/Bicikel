package si.virag.bicikelj.stations;

import android.app.Activity;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.util.DisplayUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.StationViewHolder>
{	
	public static class StationViewHolder extends RecyclerView.ViewHolder
	{
		public TextView bikeNum;
		public TextView freeSpaces;
		public TextView stationName;
		public TextView distance;

		public StationViewHolder(View view) {
			super(view);

			bikeNum = (TextView) view.findViewById(R.id.stationlist_num_full);
			freeSpaces = (TextView) view.findViewById(R.id.stationlist_num_free);
			stationName = (TextView) view.findViewById(R.id.stationlist_name);
			distance = (TextView) view.findViewById(R.id.stationlist_distance);
		}
	}
	
	private List<Station> items;
	
	public StationListAdapter(List<Station> items)
	{
		this.items = items;
		setHasStableIds(true);
	}

	
	public void updateData(StationInfo info)
	{
		if (info == null) {
			this.clearData();
			return;
		}
		
		if (info.getStations().size() != this.getItemCount())
		{
			this.items = info.getStations();
			this.notifyDataSetChanged();
			return;
		}
		
		for (Station station : info.getStations())
		{
			for (int i = 0; i < this.getItemCount(); i++)
			{
				Station s = items.get(i);
				if (s.getId() == station.getId())
				{
					s.setFreeSpaces(station.getFreeSpaces());
					s.setAvailableBikes(station.getAvailableBikes());
					break;
				}
			}
		}
		
		this.notifyDataSetChanged();
	}
	
	public void updateLocation(Location location)
	{
        if (location == null)
            return;

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
		
		this.notifyDataSetChanged();
	}
	
	public void clearData()
	{
		this.items.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public StationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stationlist_item, parent, false);
		StationViewHolder viewHolder = new StationViewHolder(view);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(StationViewHolder viewHolder, int position) {
		Station station = items.get(position);
		viewHolder.bikeNum.setText(station.getAvailableBikes() == 0 ? "Ø" : String.valueOf(station.getAvailableBikes()));
		viewHolder.freeSpaces.setText(station.getFreeSpaces() == 0 ? "Ø" : String.valueOf(station.getFreeSpaces()));
		viewHolder.stationName.setText(station.getName().replaceAll("Ø", "\n"));

		if (station.getDistance() != null)
		{
			viewHolder.distance.setText(DisplayUtils.formatDistance(station.getDistance()));
			viewHolder.distance.setVisibility(View.VISIBLE);
		}
		else
		{
			viewHolder.distance.setVisibility(View.GONE);
		}
	}

	@Override
	public long getItemId(int position) 
	{
		return this.items.get(position).getId();
	}



	@Override
	public int getItemCount() {
		return items.size();
	}
}
