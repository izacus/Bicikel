package si.virag.bicikelj.stations;

import java.util.ArrayList;

import si.virag.bicikelj.R;
import si.virag.bicikelj.data.Station;
import si.virag.bicikelj.data.StationInfo;
import si.virag.bicikelj.util.GPSManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class StationListFragment extends ListFragment implements LoaderCallbacks<StationInfo>
{
	private static final int STATION_LOADER_ID = 0;
	
	private StationListAdapter adapter = null;
	
	private GPSManager gpsManager;
	private Location location;
	
	private Animation fadeOut;
	private Animation fadeIn;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) 
	{
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
		fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
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
					Log.w(this.toString(), "Can't get current location.");
					return;
				}
				
				location = gpsManager.getCurrentLocation();
				adapter.updateLocation(location);
				adapter.notifyDataSetChanged();
			}
			
		});
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
		// Clear animation listener
		fadeOut.setAnimationListener(null);
		getListView().startAnimation(fadeIn);
		
		// Update data in-place when already available
		if (adapter.getCount() > 0)
		{
			adapter.updateData(data);
		}
		else
		{
			for (Station station : data.getStations())
			{
				adapter.add(station);
			}
		}
		
		if (location != null)
		{
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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.menu_refresh: 
				getLoaderManager().initLoader(STATION_LOADER_ID, null, StationListFragment.this).forceLoad();
				getListView().startAnimation(fadeOut);
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
