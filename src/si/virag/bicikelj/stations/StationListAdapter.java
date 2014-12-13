package si.virag.bicikelj.stations;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.events.ListItemSelectedEvent;
import si.virag.bicikelj.ui.CircleLetterView;
import si.virag.bicikelj.util.DisplayUtils;

public class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.StationListHolder>
{
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_STATION = 1;

	private List<StationListItem> items;
	
	public StationListAdapter(List<Station> items)
	{
		setHasStableIds(true);
        setItems(items);
	}

	private void setItems(List<Station> items) {
        this.items = new ArrayList<>();

        this.items.add(new StationListHeader("Priljubljene postaje"));
        this.items.add(new StationListHeader("Ostale postaje"));
        for (Station s : items) {
            this.items.add(new StationListStation(s));
        }
    }

	public void updateData(StationInfo info)
	{
		if (info == null) {
			this.clearData();
			return;
		}
		
		if (info.getStations().size() != this.getItemCount())
		{
            setItems(info.getStations());
			this.notifyDataSetChanged();
			return;
		}

        // TODO: Optimize
		for (Station station : info.getStations())
		{
			for (int i = 0; i < this.getItemCount(); i++)
			{
				StationListItem s = items.get(i);
				if (s instanceof StationListStation)
				{
                    StationListStation sls = (StationListStation)s;
                    if (sls.station.getId() == station.getId()) {
                        sls.station.setFreeSpaces(station.getFreeSpaces());
                        sls.station.setAvailableBikes(station.getAvailableBikes());
                        notifyItemChanged(i);
                    }
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

		for (StationListItem item : items)
		{
            if (item instanceof StationListStation) {
                ((StationListStation)item).station.setDistance(location);
            }
		}

        // TODO: Sort items

        this.notifyDataSetChanged();
	}
	
	public void clearData()
	{
		this.items.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public StationListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stationlist_header, parent, false);
            StationListHeaderHolder viewHolder = new StationListHeaderHolder(view);
            return viewHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stationlist_item, parent, false);
            StationListStationHolder viewHolder = new StationListStationHolder(view);
            return viewHolder;
        }
	}

	@Override
	public void onBindViewHolder(StationListHolder holder, int position) {
        StationListItem item = items.get(position);
        if (item instanceof StationListHeader) {
            StationListHeaderHolder viewHolder = (StationListHeaderHolder)holder;
            viewHolder.text.setText(((StationListHeader) item).text);
        } else {
            StationListStationHolder viewHolder = (StationListStationHolder)holder;
            Station station = ((StationListStation)item).station;
            viewHolder.bikes.setText(getFormattedNumber(station.getAvailableBikes()));
            viewHolder.free.setText(getFormattedNumber(station.getFreeSpaces()));
            viewHolder.stationName.setText(station.getName());
            viewHolder.circle.setText(station.getAbbreviation());
            viewHolder.circle.setColor(DisplayUtils.getColorFromString(station.getName()));

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

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof StationListHeader) {
            return TYPE_HEADER;
        }

        return TYPE_STATION;
    }

    private static String getFormattedNumber(int number) {
		return number == 0 ? "Ø" : String.valueOf(number);
	}


    private interface StationListItem {
        public long getId();
    };

    private static class StationListStation implements StationListItem {
        public final Station station;

        private StationListStation(Station station) {
            this.station = station;
        }

        @Override
        public long getId() {
            return station.getId();
        }
    }

    private static class StationListHeader implements StationListItem {
        public final String text;

        private StationListHeader(String text) {
            this.text = text;
        }

        @Override
        public long getId() {
            return text.hashCode();
        }
    }

    public abstract class StationListHolder extends RecyclerView.ViewHolder {
        public StationListHolder(View itemView) {
            super(itemView);
        }
    }

    public class StationListStationHolder extends StationListHolder implements View.OnClickListener {
        public final TextView free;
        public final TextView bikes;
        public final TextView stationName;
        public final TextView distance;
        public final CircleLetterView circle;

        public StationListStationHolder(View view) {
            super(view);

            View topView = view.findViewById(R.id.stationlist_item);
            topView.setOnClickListener(this);

            bikes = (TextView)view.findViewById(R.id.stationlist_bikes);
            free = (TextView)view.findViewById(R.id.stationlist_free);
            circle = (CircleLetterView)view.findViewById(R.id.stationlist_circle);

            stationName = (TextView) view.findViewById(R.id.stationlist_name);
            distance = (TextView) view.findViewById(R.id.stationlist_distance);
        }

        @Override
        public void onClick(View v) {
            StationListItem s = items.get(getPosition());
            if (s instanceof StationListStation) {
                EventBus.getDefault().post(new ListItemSelectedEvent(((StationListStation) s).station.getId()));
            }
        }
    }

    public class StationListHeaderHolder extends StationListHolder {
        public final TextView text;

        public StationListHeaderHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.stationlist_header_text);
        }
    }
}
