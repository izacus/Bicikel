package si.virag.bicikelj.stations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.station_map.StationMapActivity;
import si.virag.bicikelj.util.GPSManager;
import si.virag.bicikelj.util.ShowKeyboardRunnable;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.flurry.android.FlurryAgent;

public class StationListFragment extends SherlockListFragment implements LoaderCallbacks<StationInfo>
{
	private static final int STATION_LOADER_ID = 0;
	
	private StationListAdapter adapter = null;
	
	private GPSManager gpsManager;

	private Location location;
	
	private MenuItem searchActionView;
	
	private StationInfo data;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		gpsManager = new GPSManager();
		
		
		adapter = new StationListAdapter(getActivity(), R.layout.stationlist_item, new ArrayList<Station>());
		this.setListAdapter(adapter);
		getLoaderManager().initLoader(STATION_LOADER_ID, null, this);
	}


	@Override
	public void onPause()
	{
		super.onPause();
		gpsManager.cancelSearch();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		gpsManager.findCurrentLocation(getActivity(), new Handler() 
		{

			@Override
			public void handleMessage(Message msg)
			{
				if (msg.what != GPSManager.GPS_LOCATION_OK)
				{
					FlurryAgent.logEvent("LocationNotAvailable");
					Log.w(this.toString(), "Can't get current location.");
					return;
				}
				
				location = gpsManager.getCurrentLocation();
				gpsManager.cancelSearch();
				refreshAdapterLocation();
			}
			
		});
	}

	public void refreshAdapterLocation()
	{
		if (adapter != null && location != null)
		{
			adapter.updateLocation(location);
		}
		
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
							 ViewGroup container,
							 Bundle savedInstanceState) 
	{
		return inflater.inflate(R.layout.stationlist_fragment, container);
	}

	@Override
	public Loader<StationInfo> onCreateLoader(int id, Bundle args) 
	{
		return new JSONInformationDataLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<StationInfo> loader, StationInfo data) 
	{
		this.data = data;
		
		// Update data in-place when already available
		adapter.updateData(data);
		
		if (location != null)
		{
			FlurryAgent.logEvent("LocationRetrievedBeforeLoad");
			adapter.updateLocation(location);
		}
		
		adapter.notifyDataSetChanged();
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
		final EditText searchBox = (EditText) searchItem.getActionView().findViewById(R.id.search_box);
		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			
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
				refreshAdapterLocation();				
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
			if (this.searchActionView.isActionViewExpanded()) {
				this.searchActionView.collapseActionView();
			}
			else
			{
				this.searchActionView.expandActionView();
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
			
			refreshAdapterLocation();
		}
		else
		{
			adapter.updateData(data);
			refreshAdapterLocation();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (this.data == null)
		{
			return false;
		}
		
		switch (item.getItemId())
		{
			case R.id.menu_refresh:
				this.adapter.clearData();
				this.adapter.notifyDataSetChanged();
				this.data = null;
				getLoaderManager().restartLoader(STATION_LOADER_ID, null, StationListFragment.this);
				FlurryAgent.logEvent("StationListRefresh");
				break;
			case R.id.menu_map:
				showFullMap();
				FlurryAgent.logEvent("FullMap");
				break;
			default:
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		
		ArrayList<Station> station = new ArrayList<Station>();
		station.add(data.getStations().get(position));
		//
		Map<String, String> params = new HashMap<String, String>();
		params.put("Station", data.getStations().get(position).getName());
		FlurryAgent.logEvent("StationListTap", params);
		//
		Intent intent = new Intent(getActivity(), StationMapActivity.class);
		intent.putExtras(packStationData(station));
		startActivity(intent);
	}
	
	private void showFullMap()
	{
		if (this.data == null)
			return;
		
		Intent intent = new Intent(getActivity(), StationMapActivity.class);
		intent.putExtras(packStationData(data.getStations()));
		startActivity(intent);
	}
	
	
	private static Bundle packStationData(List<Station> data)
	{
		int stationNum = data.size();
		
		double[] lngs = new double[stationNum];
		double[] lats = new double[stationNum];
		String[] names = new String[stationNum];
		int[] frees = new int[stationNum];
		int[] fulls = new int[stationNum];
		
		for (int i = 0; i < stationNum; i++)
		{
			Station station = data.get(i);
			lngs[i] = station.getLocation().getLongitude();
			lats[i] = station.getLocation().getLatitude();
			names[i] = station.getName();
			frees[i] = station.getFreeSpaces();
			fulls[i] = station.getAvailableBikes();
		}
		
		Bundle targetBundle = new Bundle();
		targetBundle.putDoubleArray("lats", lats);
		targetBundle.putDoubleArray("lngs", lngs);
		targetBundle.putStringArray("names", names);
		targetBundle.putIntArray("frees", frees);
		targetBundle.putIntArray("fulls", fulls);
		
		return targetBundle;
	}
}
