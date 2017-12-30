package si.virag.bicikelj.stations;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.events.ListItemSelectedEvent;
import si.virag.bicikelj.ui.CircleLetterView;
import si.virag.bicikelj.util.DisplayUtils;
import si.virag.bicikelj.util.FavoritesManager;
import si.virag.bicikelj.util.FuzzyDateTimeFormatter;

public final class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.StationListHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_STATION = 1;
    private static final int TYPE_TEXT = 2;

    private final FavoritesManager favManager;
    private final Context ctx;

    private List<Station> stations;
    private List<StationListItem> items;
    private Calendar updateTime;

    StationListAdapter(Context ctx, FavoritesManager fm, List<Station> items, Calendar updateTime) {
        this.ctx = ctx;
        setHasStableIds(true);
        setItems(items);
        this.favManager = fm;
        this.updateTime = updateTime;
    }

    private void setItems(List<Station> items) {
        if (items.size() == 0) {
            this.items = new ArrayList<>();
            this.stations = new ArrayList<>();
            notifyDataSetChanged();
            return;
        }

        this.stations = items;
        this.items = new ArrayList<>();

        ArrayList<Station> favorites = new ArrayList<>();
        ArrayList<Station> others = new ArrayList<>();

        for (Station item : items) {
            if (favManager.isFavorite(item.getId()))
                favorites.add(item);
            else
                others.add(item);
        }

        Collections.sort(favorites, (lhs, rhs) -> {
            if (lhs == null) return 1;
            if (rhs == null) return -1;

            if (lhs.getDistance() == null)
                return 1;
            if (rhs.getDistance() == null)
                return -1;
            return lhs.getDistance().compareTo(rhs.getDistance());
        });

        Collections.sort(others, (lhs, rhs) -> {
            if (lhs == null) return 1;
            if (rhs == null) return -1;

            if (lhs.getDistance() == null)
                return 1;
            if (rhs.getDistance() == null)
                return -1;

            return lhs.getDistance().compareTo(rhs.getDistance());
        });

        if (updateTime != null)
            this.items.add(new StationListText(ctx.getString(R.string.data_is) + " " + FuzzyDateTimeFormatter.getTimeAgo(ctx, updateTime)));

        this.items.add(new StationListHeader(ctx.getString(R.string.stationlist_header_favorites)));

        if (favorites.size() > 0) {
            for (Station s : favorites) {
                this.items.add(new StationListStation(s));
            }
        } else {
            this.items.add(new StationListText(ctx.getString(R.string.stationlist_hint_favorites)));
        }

        this.items.add(new StationListHeader(ctx.getString(R.string.stationlist_header_other_stations)));
        for (Station s : others) {
            this.items.add(new StationListStation(s));
        }

        this.items.add(new StationListText(ctx.getString(R.string.source)));

        notifyDataSetChanged();
    }

    public void updateData(StationInfo info) {
        if (info == null) {
            this.clearData();
            return;
        }

        this.updateTime = info.getTimeUpdated();

        if (info.getStations().size() != this.getItemCount()) {
            setItems(info.getStations());
            this.notifyDataSetChanged();
            return;
        }

        setItems(info.getStations());
    }

    public void updateLocation(Location location) {
        if (location == null)
            return;

        for (Station item : stations) {
            item.setDistance(location);
        }

        setItems(stations);
    }

    private void clearData() {
        this.items.clear();
        this.stations.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public StationListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stationlist_header, parent, false);
            return new StationListHeaderHolder(view);
        } else if (viewType == TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stationlist_text, parent, false);
            return new StationListTextHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stationlist_item, parent, false);
            return new StationListStationHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(StationListHolder holder, int position) {
        StationListItem item = items.get(position);
        if (item instanceof StationListHeader) {
            StationListHeaderHolder viewHolder = (StationListHeaderHolder) holder;
            viewHolder.text.setText(((StationListHeader) item).text);
        } else if (item instanceof StationListText) {
            StationListTextHolder viewHolder = (StationListTextHolder) holder;
            viewHolder.text.setText(((StationListText) item).text);
        } else {
            StationListStationHolder viewHolder = (StationListStationHolder) holder;
            Station station = ((StationListStation) item).station;
            viewHolder.bikes.setText(getFormattedNumber(station.getAvailableBikes()));
            viewHolder.free.setText(getFormattedNumber(station.getFreeSpaces()));
            viewHolder.stationName.setText(station.getName());
            viewHolder.circle.setText(station.getAbbreviation());
            viewHolder.circle.setColor(DisplayUtils.getColorFromString(station.getName()));

            if (station.getDistance() != null) {
                viewHolder.distance.setText(DisplayUtils.formatDistance(station.getDistance()));
                viewHolder.distance.setVisibility(View.VISIBLE);
            } else {
                viewHolder.distance.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public long getItemId(int position) {
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
        } else if (items.get(position) instanceof StationListText) {
            return TYPE_TEXT;
        }

        return TYPE_STATION;
    }

    private static String getFormattedNumber(int number) {
        return number == 0 ? "Ø" : String.valueOf(number);
    }


    private interface StationListItem {
        long getId();
    }

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

    private static class StationListText implements StationListItem {
        public final CharSequence text;

        private StationListText(CharSequence text) {
            this.text = text;
        }

        @Override
        public long getId() {
            return "t".hashCode() + text.hashCode();
        }
    }

    public abstract class StationListHolder extends RecyclerView.ViewHolder {
        public StationListHolder(View itemView) {
            super(itemView);
        }
    }

    public final class StationListStationHolder extends StationListHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
        final TextView free;
        final TextView bikes;
        final TextView stationName;
        final TextView distance;
        final CircleLetterView circle;

        StationListStationHolder(View view) {
            super(view);

            View topView = view.findViewById(R.id.stationlist_item);
            topView.setOnClickListener(this);
            topView.setOnCreateContextMenuListener(this);

            bikes = view.findViewById(R.id.stationlist_bikes);
            free = view.findViewById(R.id.stationlist_free);
            circle = view.findViewById(R.id.stationlist_circle);

            stationName = view.findViewById(R.id.stationlist_name);
            distance = view.findViewById(R.id.stationlist_distance);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos >= items.size()) return;
            StationListItem s = items.get(getAdapterPosition());
            if (s instanceof StationListStation) {
                EventBus.getDefault().post(new ListItemSelectedEvent(((StationListStation) s).station.getId()));
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            final StationListItem s = items.get(getAdapterPosition());
            if (s instanceof StationListStation) {
                String text = favManager.isFavorite(s.getId()) ? ctx.getString(R.string.stationlist_menu_add_favorites) : ctx.getString(R.string.stationlist_menu_remove_favorites);
                MenuItem item = menu.add(text);

                item.setOnMenuItemClickListener(item1 -> {
                    if (favManager.isFavorite(s.getId()))
                        favManager.removeFavorite(s.getId());
                    else
                        favManager.setFavorite(s.getId());
                    setItems(stations);
                    return true;
                });
            }
        }
    }

    public final class StationListHeaderHolder extends StationListHolder {
        public final TextView text;

        public StationListHeaderHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.stationlist_header_text);
        }
    }

    public final class StationListTextHolder extends StationListHolder {
        public final TextView text;


        public StationListTextHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.stationlist_text);
        }
    }
}
