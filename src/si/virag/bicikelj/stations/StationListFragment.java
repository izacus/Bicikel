package si.virag.bicikelj.stations;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.events.FocusOnStationEvent;
import si.virag.bicikelj.events.ListItemSelectedEvent;
import si.virag.bicikelj.events.LocationUpdatedEvent;
import si.virag.bicikelj.events.StationDataUpdatedEvent;
import si.virag.bicikelj.station_map.StationMapActivity;
import si.virag.bicikelj.stations.api.CityBikesApi;
import si.virag.bicikelj.stations.api.CityBikesApiClient;
import si.virag.bicikelj.util.DividerItemDecoration;
import si.virag.bicikelj.util.FavoritesManager;
import si.virag.bicikelj.util.GPSUtil;
import si.virag.bicikelj.util.ShowKeyboardRunnable;

public class StationListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView listView;

	private StationListAdapter adapter = null;
	private MenuItem searchActionView;
	private StationInfo data;
	
	private boolean error = false;
    private boolean isTablet;
    private Location location;

    private FavoritesManager fm;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        fm = new FavoritesManager(getActivity());
		adapter = new StationListAdapter(getActivity(), fm, new ArrayList<Station>(), null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

        isTablet = ((MainActivity)getActivity()).isTabletLayout();
        refresh();
	}

    @Override
    public void onStart()
    {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container,
							 Bundle savedInstanceState) 
	{
		View v = inflater.inflate(R.layout.stationlist_fragment, container);
		listView = (RecyclerView) v.findViewById(R.id.stationlist_list);
		listView.setAdapter(adapter);
		listView.setHasFixedSize(true);
		listView.setLayoutManager(new LinearLayoutManager(getActivity()));
		listView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.stationlist_swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorScheme(R.color.primary,
                                          R.color.primary_dark,
                                          R.color.secondary,
                                          R.color.primary_dark
                );

        swipeRefreshLayout.setRefreshing(true);

        return v;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_stationlist, menu);
		
		final MenuItem searchItem = menu.findItem(R.id.menu_search);
		this.searchActionView = searchItem;

        final EditText searchBox = (EditText) MenuItemCompat.getActionView(searchItem).findViewById(R.id.search_box);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener()
        {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) 
			{
				if (StationListFragment.this.data == null)
					return false;
				
				searchBox.post(new ShowKeyboardRunnable(getActivity(), searchBox));
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				adapter.updateData(data);
                if (location != null)
                    adapter.updateLocation(location);

				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
				return true;
			}
		});
		
		searchBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				filterStations(s.toString());
			}
		});
	}

	public void searchRequested()
	{
		if (this.searchActionView != null)
		{
            if (MenuItemCompat.isActionViewExpanded(searchActionView))
            {
                MenuItemCompat.collapseActionView(searchActionView);
            }
            else
            {
                MenuItemCompat.expandActionView(searchActionView);
            }
		}
	}
	
	private void filterStations(String text)
	{
		Log.d(this.toString(), "Filter: " + text);
		
		if (text.trim().length() > 0)
		{
			StationInfo filteredInfo = data.getFilteredInfo(text);
			
			if (filteredInfo.getStations().size() > 0)
				adapter.updateData(filteredInfo);
			else
				adapter.updateData(data);
			
		}
		else
		{
			adapter.updateData(data);
		}

        if (location != null)
           adapter.updateLocation(location);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (this.data == null)
		{
			return true;
		}
		
		switch (item.getItemId())
		{
			case R.id.menu_map:
				showFullMap();
				break;
			default:
				return false;
		}
		
		return true;
	}
	
	private void refresh()
	{
        swipeRefreshLayout.setRefreshing(true);
		this.data = null;
        CityBikesApi api = CityBikesApiClient.getBicikeljApi();
        api.getStationData(new Callback<StationInfo>() {
            @Override
            public void success(StationInfo data, Response response) {
                swipeRefreshLayout.setRefreshing(false);
                StationListFragment.this.data = data;
                // Update data in-place when already available
                adapter.updateData(data);

                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.findViewById(R.id.stationlist_emptyview).setVisibility(View.INVISIBLE);
                }

                if (location != null)
                    adapter.updateLocation(location);

                if (data == null)
                {
                    showError();
                    return;
                }

                EventBus.getDefault().postSticky(new StationDataUpdatedEvent(data.getStations()));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Bicikelj", "Load failed.", error.getCause());
                showError();
            }
        });
	}
	
	private void showError() {
		this.error = true;
		TextView text = (TextView) getActivity().findViewById(R.id.stationlist_loading_error);
        text.setVisibility(View.VISIBLE);
        TextView loadingText = (TextView) getActivity().findViewById(R.id.stationlist_loading_text);
        loadingText.setVisibility(View.INVISIBLE);
		ProgressBar progress = (ProgressBar) getActivity().findViewById(R.id.stationlist_loading_progress);
		progress.setVisibility(View.INVISIBLE);
		text.setText(R.string.stationlist_load_error);
	}
	
	private void showFullMap()
	{
        if (!GPSUtil.checkPlayServices(getActivity()))
            return;

		if (this.data == null)
			return;
		
		Intent intent = new Intent(getActivity(), StationMapActivity.class);
		startActivity(intent);
        getActivity().overridePendingTransition(R.anim.slide_in_right, 0);
    }


    @Override
    public void onRefresh()
    {
        refresh();
    }

    public void onEventMainThread(ListItemSelectedEvent e) {
        if (!GPSUtil.checkPlayServices(getActivity()))
            return;

        if (isTablet)
        {
            EventBus.getDefault().post(new FocusOnStationEvent(e.stationId));
        }
        else
        {
            Intent intent = new Intent(getActivity(), StationMapActivity.class);
            intent.putExtra("focusOnStation", e.stationId);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    public void onEventMainThread(LocationUpdatedEvent event) {
        this.location = event.location;
        if (adapter != null) adapter.updateLocation(location);
    }
}
