package si.virag.bicikelj.stations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import de.greenrobot.event.EventBus;
import si.virag.bicikelj.MainActivity;
import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.events.FocusOnStationEvent;
import si.virag.bicikelj.events.ListItemSelectedEvent;
import si.virag.bicikelj.events.StationDataUpdatedEvent;
import si.virag.bicikelj.station_map.StationMapActivity;
import si.virag.bicikelj.util.DividerItemDecoration;
import si.virag.bicikelj.util.GPSUtil;
import si.virag.bicikelj.util.ShowKeyboardRunnable;

import java.util.ArrayList;

public class StationListFragment extends Fragment implements LoaderCallbacks<StationInfo>, GooglePlayServicesClient.ConnectionCallbacks, SwipeRefreshLayout.OnRefreshListener
{
	private static final int STATION_LOADER_ID = 0;

    private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView listView;

	private StationListAdapter adapter = null;
	private MenuItem searchActionView;
	private StationInfo data;
	
	private boolean error = false;
    private LocationClient locationClient = null;
    private boolean isTablet;


    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new StationListAdapter(new ArrayList<Station>());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

        isTablet = ((MainActivity)getActivity()).isTabletLayout();
		getLoaderManager().initLoader(STATION_LOADER_ID, null, this);

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) == ConnectionResult.SUCCESS)
        {
            locationClient = new LocationClient(getActivity(), this, null);
        }
	}

    @Override
    public void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
        if (locationClient != null)
            locationClient.connect();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (locationClient != null)
            locationClient.disconnect();
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
	public Loader<StationInfo> onCreateLoader(int id, Bundle args) 
	{
		return new JSONInformationDataLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<StationInfo> loader, StationInfo data) 
	{
        swipeRefreshLayout.setRefreshing(false);
		this.data = data;
		// Update data in-place when already available
		adapter.updateData(data);
		getActivity().findViewById(R.id.stationlist_emptyview).setVisibility(View.INVISIBLE);

        if (locationClient != null && locationClient.isConnected())
            adapter.updateLocation(locationClient.getLastLocation());

        if (data == null)
        {
			showError();
			return;
		}

        EventBus.getDefault().postSticky(new StationDataUpdatedEvent(data.getStations()));
	}

	@Override
	public void onLoaderReset(Loader<StationInfo> loader) 
	{
		// TODO Auto-generated method stub
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
                if (locationClient != null)
                    adapter.updateLocation(locationClient.getLastLocation());

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

        if (locationClient != null && locationClient.isConnected())
            adapter.updateLocation(locationClient.getLastLocation());
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
		getLoaderManager().restartLoader(STATION_LOADER_ID, null, StationListFragment.this);
	}
	
/*	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);



        if (!GPSUtil.checkPlayServices(getActivity()))
            return;


        Station s = adapter.getItem(position);

        if (isTablet)
        {
            EventBus.getDefault().post(new FocusOnStationEvent(s.getId()));
        }
        else
        {
            Intent intent = new Intent(getActivity(), StationMapActivity.class);
            intent.putExtra("focusOnStation", s.getId());
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

	} */
	
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
        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        if (adapter != null)
            adapter.updateLocation(locationClient.getLastLocation());
    }

    @Override
    public void onDisconnected()
    {

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
}
